package proxyLB;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ini {

	private static Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");

	private static Map<String, String> _entries = new HashMap<>();

	public Ini(String path) {
	}

	public static Map<String, String> load(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;

			while ((line = br.readLine()) != null) {
				Matcher m = _keyValue.matcher(line);
				if (m.matches()) {
					String key = m.group(1).trim();
					String value = m.group(2).trim();
					_entries.put(key, value);
				}
			}
			return _entries;
		}
	}
}
