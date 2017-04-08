package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ReaderInput;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A configurable parser of TOML configurations.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml">TOML specification</a>
 */
public final class TomlParser implements ConfigParser<TomlConfig, Config> {
	// --- Parser's settings ---
	private int initialStringBuilderCapacity = 16, initialListCapacity = 10;
	private boolean lenientBareKeys = false;

	// --- Parser's methods ---
	@Override
	public TomlConfig parse(Reader reader) {
		return parse(new ReaderInput(reader), new TomlConfig());
	}

	@Override
	public void parse(Reader reader, Config destination) {
		parse(new ReaderInput(reader), destination);
	}

	private <T extends Config> T parse(CharacterInput input, T destination) {
		T rootTable = TableParser.parseNormal(input, this, destination);
		int next;
		while ((next = input.peek()) != -1) {
			if (next == '[') {//[[ element of an array of tables
				input.skipPeeks();
				List<String> path = TableParser.parseTableArrayName(input, this);
				int lastIndex = path.size() - 1;
				String lastKey = path.get(lastIndex);
				Map<String, Object> parentMap = getSubTableMap(rootTable, path.subList(0, lastIndex));

				TomlConfig table = TableParser.parseNormal(input, this);
				List<TomlConfig> arrayOfTables = (List)parentMap.get(lastKey);
				if (arrayOfTables == null) {
					arrayOfTables = new ArrayList<>(initialListCapacity);
					parentMap.put(lastKey, arrayOfTables);
				}
				arrayOfTables.add(table);
			} else {//[ a table
				List<String> path = TableParser.parseTableName(input, this);
				int lastIndex = path.size() - 1;
				String lastKey = path.get(lastIndex);
				Map<String, Object> parentMap = getSubTableMap(rootTable, path.subList(0, lastIndex));

				Object alreadyDeclared = parentMap.get(lastKey);
				if (alreadyDeclared == null) {
					TomlConfig table = TableParser.parseNormal(input, this);
					parentMap.put(lastKey, table);
				} else {
					if (alreadyDeclared instanceof Config) {
						TableParser.parseNormal(input, this, (Config)alreadyDeclared);
					} else {
						throw new ParsingException("Entry " + path + " has been defined twice.");
					}
				}
			}
		}
		return rootTable;
	}

	private Map<String, Object> getSubTableMap(Config parentTable, List<String> path) {
		if (path.isEmpty()) {
			return parentTable.asMap();
		}
		Map<String, Object> currentMap = parentTable.asMap();
		for (String key : path) {
			Object value = currentMap.get(key);
			if (value == null) {
				Config sub = new TomlConfig();
				currentMap.put(key, sub);
				currentMap = sub.asMap();
			} else if (value instanceof Config) {
				currentMap = ((Config)value).asMap();
			} else if (value instanceof List) {
				List<?> list = (List<?>)value;
				if (!list.isEmpty() && list.get(0) instanceof Config) {// Arrays of tables
					int lastIndex = list.size() - 1;
					currentMap = ((Config)list.get(lastIndex)).asMap();
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		return currentMap;
	}

	// --- Getters/setters for the settings ---
	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	public void setLenientWithBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
	}

	public int getInitialStringBuilderCapacity() {
		return initialStringBuilderCapacity;
	}

	public void setInitialStringBuilderCapacity(int initialStringBuilderCapacity) {
		this.initialStringBuilderCapacity = initialStringBuilderCapacity;
	}

	public int getInitialListCapacity() {
		return initialListCapacity;
	}

	public void setInitialListCapacity(int initialListCapacity) {
		this.initialListCapacity = initialListCapacity;
	}
}