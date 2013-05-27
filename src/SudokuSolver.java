import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * algorithm based on Norvig solution for sudoku solver:
 * http://norvig.com/sudoku.html
 * 
 * @author joosep
 * 
 */
public class SudokuSolver {
	String[] concat(String[] A, String[] B) {
		int aLen = A.length;
		int bLen = B.length;
		String[] C = new String[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	private String[] cross(String a, String b) {

		// Cross product of elements in A and elements in B.
		List<String> crossProduct = new ArrayList<String>();
		for (int i = 0; i < a.length(); i++) {
			for (int j = 0; j < b.length(); j++) {
				crossProduct.add(a.charAt(i) + "" + b.charAt(j));
			}
		}
		return crossProduct.toArray(new String[0]);
	}

	private String[][] createUnitList(String rows, String cols) {
		List<String[]> list = new ArrayList<String[]>();
		for (int i = 0; i < cols.length(); i++) {
			list.add(cross(rows, cols.charAt(i) + ""));
		}
		for (int i = 0; i < rows.length(); i++) {
			list.add(cross(rows.charAt(i) + "", cols));
		}
		for (String cs : new String[] { "123", "456", "789" }) {
			for (String rs : new String[] { "ABC", "DEF", "GHI" }) {
				list.add(cross(rs, cs));
			}
		}
		return list.toArray(new String[0][0]);
	}

	private Map<String, String[][]> createUnitList(String[] squares,
			String[][] unitList) {
		Map<String, String[][]> map = new HashMap<String, String[][]>();
		for (String s : squares) {
			List<String[]> unit = new ArrayList<String[]>();
			for (String[] u : unitList) {
				if (Arrays.asList(u).contains(s)) {
					unit.add(u);
				}
			}
			map.put(s, unit.toArray(new String[0][0]));
		}
		return map;
	}

	private Map<String, String[]> createPeersList(
			Map<String, String[][]> units, String[] squares) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String s : squares) {
			String[][] unit = units.get(s);
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < unit.length; i++) {
				for (int j = 0; j < unit[i].length; j++) {
					if (!unit[i][j].equals(s) && !list.contains(unit[i][j])) {
						list.add(unit[i][j]);
					}
				}
			}
			map.put(s, list.toArray(new String[0]));
		}
		return map;
	}

	String digits = "123456789";
	String rows = "ABCDEFGHI";
	String cols = digits;
	String[] squares = cross(rows, cols);
	String[][] unitList = createUnitList(rows, cols);
	Map<String, String[][]> units = createUnitList(squares, unitList);
	Map<String, String[]> peers = createPeersList(units, squares);

	/**
	 * Convert grid into a map of {square: char} with '0' or '.' for empties.
	 * 
	 * @param grid
	 * @return
	 */
	Map<String, String> gridValues(String grid) {
		Map<String, String> gridMap = new HashMap<String, String>();
		for (int i = 0; i < grid.length(); i++) {
			if (digits.indexOf(grid.charAt(i)) == -1 && grid.charAt(i) != '0'
					&& grid.charAt(i) != '.') {
				return null;
			}
			gridMap.put(squares[i], grid.charAt(i) + "");
		}
		return gridMap;
	}

	/**
	 * Convert grid to a map of possible values, {square: digits}, or return
	 * null if a contradiction is detected.
	 * 
	 * @param grid
	 * @return
	 */
	Map<String, String> parseGrid(String grid) {
		Map<String, String> values = new HashMap<String, String>();
		try {
			for (String s : squares) {
				values.put(s, digits);
			}
			for (Entry<String, String> e : gridValues(grid).entrySet()) {
				if (digits.contains(e.getValue()) && assign(values, e.getKey(), e.getValue()) == null) {
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("not correct grid: " + grid);
			return null;
		}
		return values;
	}

	/**
	 * Eliminate all the other values (except d) from values[s] and propagate.
	 * Return values, except return null if a contradiction is detected
	 * 
	 * @param values
	 * @param s
	 * @param d
	 * @return
	 */
	Map<String, String> assign(Map<String, String> values, String s, String d) {
		String otherValues = values.get(s).replace(d, "");
		for (int i = 0; i < otherValues.length(); i++) {
			if (eliminate(values, s, otherValues.charAt(i) + "") == null) {
				return null;
			}
		}
		return values;
	}
	/**
	 * Eliminate d from values[s]; propagate when values or places <= 2. Return
	 * values, except return null if a contradiction is detected.
	 * 
	 * @param values
	 * @param s
	 * @param d
	 * @return
	 */
	Map<String, String> eliminate(Map<String, String> values, String s, String d) {
		if (!values.get(s).contains(d)) {
			return values;
		}
		values.put(s, values.get(s).replace(d, ""));
		// (1) If a square s is reduced to one value d2, then eliminate d2 from
		// the peers.
		if (values.get(s).length() == 0) {
			return null; // Contradiction: removed last value
		} else if (values.get(s).length() == 1) {
			String d2 = values.get(s);
			for (String s2 : peers.get(s)) {
				if (eliminate(values, s2, d2) == null) {
					return null;
				}
			}
		}

		// (2) If a unit u is reduced to only one place for a value d, then
		// put it there.
		for (String[] u : units.get(s)) {
			List<String> dplaces = new ArrayList<String>();
			for (String s1 : u) {
				if (values.get(s1).contains(d)) {
					dplaces.add(s1);
				}
			}
			if (dplaces.size() == 0) {
				return null;// Contradiction: no place for this value
			} else if (dplaces.size() == 1) {
				// d can only be in one place in unit; assign it there
				if (assign(values, dplaces.get(0), d) == null) {
					return null;
				}
			}

		}
		return values;
	}

	/**
	 * Display these values as a 2-D grid.
	 * 
	 * @param values
	 */
	void print(Map<String, String> values) {

		for (int i = 0; i < rows.length(); i++) {
			for (int j = 0; j < cols.length(); j++) {
				System.out.print(values.get(rows.charAt(i) + ""
						+ cols.charAt(j))
						+ " ");
				if (j == 2 || j == 5) {
					System.out.print("|");
				}
			}
			System.out.println();
			if (i == 2 || i == 5) {
				System.out.println("------+------+------");
			}
		}
	}

	Map<String, String> solve(String grid) {
		return search(parseGrid(grid));
	}

	/**
	 * Using depth-first search and propagation, try all possible values.
	 * 
	 * @param values
	 * @return
	 */
	Map<String, String> search(Map<String, String> values) {
		if (values == null) {
			return null;
		}
		boolean isSolved = true;
		for (String s : squares) {
			if (values.get(s).length() != 1) {
				isSolved = false;
			}
		}
		if (isSolved) {
			return values;
		}
		// Chose the unfilled square s with the fewest possibilities
		String minS = "";
		int minN = Integer.MAX_VALUE;
		for (String s : squares) {
			int n = values.get(s).length();
			if (n > 1) {
				if (n < minN) {
					minN = n;
					minS = s;
				}
			}
		}
		for (int i = 0; i < values.get(minS).length(); i++) {
			Map<String, String> result = search(assign(
					new HashMap<String, String>(values), minS, values.get(minS)
							.charAt(i) + ""));
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Attempt to solve a sequence of grids. Report results. When showif is a
	 * number of seconds, display puzzles that take longer. When showif is null,
	 * don't display any puzzles.
	 * 
	 * @param grids
	 * @param name
	 * @param showIf
	 */
	void solveAll(String[] grids, String name, Double showIf) {
		double[] times = new double[grids.length];
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		for (int i = 0; i < grids.length; i++) {
			long start = System.currentTimeMillis();
			Map<String, String> values = solve(grids[i]);
			long t = System.currentTimeMillis() - start;
			if (values != null) {
				results.add(values);
				times[i] = t;
			}
			if (showIf != null && t > showIf) {
				print(gridValues(grids[i]));
				if (values != null) {
					System.out.println();
					print(values);
				}
				System.out.println("time: " + t + " ms");
				System.out.println();
			}
		}
		if (results.size() > 1) {

			System.out.println("Solved " + results.size() + " of "
					+ grids.length + " " + name + " puzzles. (avg: "
					+ sum(times) / grids.length + " ms, max " + max(times)
					+ " ms)");
		}
	}

	double sum(double[] array) {
		double sum = 0.0;
		for (double d : array) {
			sum += d;
		}
		return sum;
	}

	double max(double[] array) {
		double max = 0.0;
		for (double d : array) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	/**
	 * A puzzle is solved if each unit is a permutation of the digits 1 to 9.
	 * 
	 * @param values
	 */
	Map<String, String> solved(Map<String, String> values) {
		if (values != null) {
			for (String[] u : unitList) {
				if (!unitSolved(u, values)) {
					return null;
				}
			}
			return values;
		}
		return null;
	}

	boolean unitSolved(String[] unit, Map<String, String> values) {
		String removingDigits = digits;
		for (String s : unit) {
			if (removingDigits.contains(values.get(s))) {
				removingDigits.replace(values.get(s), "");
			} else {
				return false;
			}
		}
		if (removingDigits.length() != 0) {
			return false;
		}
		return true;
	}

	static String[] fromFile(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)));
		List<String> list = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		br.close();
		return list.toArray(new String[0]);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String grid1 = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
		String grid2 = "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";
		String hard1 = ".....6....59.....82....8....45........3........6..3.54...325..6..................";
		String[] array = new String[] { grid1, grid2, hard1 };
		SudokuSolver solver = new SudokuSolver();
		//solver.solveAll(array, "test", 0.0);
		 // solver.solveAll(fromFile("top100.txt"), "hard", 0.0);
		solver.solveAll(fromFile("top2365.txt"), "hard", 100.0);
	}
}
