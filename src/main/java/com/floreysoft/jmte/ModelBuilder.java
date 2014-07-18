package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for simple model creation.
 * 
 * <p>
 * Sample usage
 * 
 * <pre>
 * Map&lt;String, Object&gt; model = new ModelBuilder(&quot;1&quot;, &quot;arg1&quot;, &quot;2&quot;, &quot;arg2&quot;).build();
 * </pre>
 * 
 * </p>
 */
public final class ModelBuilder {
	private final Map<String, Object> model = new HashMap<String, Object>();

	public ModelBuilder(String name1, Object value1) {
		model.put(name1, value1);
	}

	public ModelBuilder(String name1, Object value1, String name2, Object value2) {
		model.put(name1, value1);
		model.put(name2, value2);
	}

	public ModelBuilder(String name1, Object value1, String name2,
			Object value2, String name3, Object value3) {
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
	}

	public ModelBuilder(String name1, Object value1, String name2,
			Object value2, String name3, Object value3, String name4,
			Object value4) {
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
	}

	public ModelBuilder(String name1, Object value1, String name2,
			Object value2, String name3, Object value3, String name4,
			Object value4, String name5, Object value5) {
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
		model.put(name5, value4);
	}

	/**
	 * Transforms an array to a model using the index of the elements (starting
	 * from 1) in the array and a prefix to form their names.
	 * 
	 * @param prefix
	 *            the prefix to add to the index or <code>null</code> if none
	 *            shall be applied
	 * @param args
	 *            the array to be transformed into the model
	 * @return the model containing the arguments
	 */
	public ModelBuilder(String prefix, Object... args) {
		if (prefix == null) {
			prefix = "";
		}
		for (int i = 0; i < args.length; i++) {
			Object value = args[i];
			String name = prefix + (i + 1);
			model.put(name, value);
		}
	}

	/**
	 * Merges any number of named lists into a single one containing their
	 * combined values. Can be very handy in case of a servlet request which
	 * might contain several lists of parameters that you want to iterate over
	 * in a combined way.
	 * 
	 * @param names
	 *            the names of the variables in the following lists
	 * @param lists
	 *            the lists containing the values for the named variables
	 * @return a merge list containing the combined values of the lists
	 */
	public static List<Map<String, Object>> mergeLists(String[] names,
			List<Object>... lists) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (lists.length != 0) {

			// first check if all looks good
			int expectedSize = lists[0].size();
			for (int i = 1; i < lists.length; i++) {
				List<Object> list = lists[i];
				if (list.size() != expectedSize) {
					throw new IllegalArgumentException(
							"All lists and array of names must have the same size!");
				}
			}

			// yes, things are ok
			List<Object> masterList = lists[0];
			for (int i = 0; i < masterList.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 0; j < lists.length; j++) {
					String name = names[j];
					List<Object> list = lists[j];
					Object value = list.get(i);
					map.put(name, value);
				}
				resultList.add(map);
			}
		}
		return resultList;
	}

	public Map<String, Object> build() {
		return model;
	}
}
