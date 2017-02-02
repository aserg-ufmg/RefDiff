/*
 * Created on May 7, 2004
 */
package tyRuBa.applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import tyRuBa.engine.FrontEnd;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

/**
 * @author dsjanzen
 * 
 * tyrubadoc(?Group, ?Pred, ?Description)
 * tyrubadocGroup(?Order, ?ID, ?Label, ?Description)
 */
public class TyrubaDoc {
	
	private static PrintWriter out;
	private static Set groups = new TreeSet();
	private static File header = null;
	private static File footer = null;
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			usage();
			System.exit(1);
		}
		
		
		if (args.length > 2) {
			header = new File(args[2]);
		}

		if (args.length > 3) {
			footer = new File(args[3]);
		}

		try {
			
			// Create writer
			File fout = new File(args[1]);
			
			out = new PrintWriter(new FileOutputStream(fout));

			// Open and initialize tyruba connection 
			FrontEnd frontEnd = new FrontEnd(true);
			File fin = new File(args[0]);
			frontEnd.load(fin.toString());
			Connection con = new Connection(frontEnd);

			// Query groups
			PreparedQuery query = con.prepareQuery("tyrubadocGroup(?Order, ?ID, ?Label, ?Description)");
			ResultSet rs = query.executeQuery();
		
			while (rs.next()) {
				String id = rs.getString("?ID");
				Integer order = new Integer(rs.getInt("?Order"));
				String label = rs.getString("?Label");
				String description = rs.getString("?Description");
				Group g = new Group(id, order, label, description);
				groups.add(g);
			}

			// Query for documentation
			query = con.prepareQuery("tyrubadoc(?Group, ?Pred, ?Description)");
			rs = query.executeQuery();
		
			// Output HTML
			
			printHeader();
			
			
			// Create a map for each group of rules
			
			Map groupRules = new TreeMap();
			
			while (rs.next()) {
				String group = rs.getString("?Group");
				String rule = rs.getString("?Pred");
				String doc = rs.getString("?Description");
				Map rules = (Map) groupRules.get(group);
				if (rules == null) {
					rules = new TreeMap();
					groupRules.put(group, rules);
				}
				rules.put(rule, doc);
			}

			// Loop through groups 
			
			Iterator iter = groups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				Map rules = (Map) groupRules.remove(group.id);
				if (rules == null) {
					rules = new TreeMap();
				}
				printGroup(group, rules);
			}
			
			// Print out any leftovers
			
			iter = groupRules.keySet().iterator();
			while (iter.hasNext()) {
				String id = (String) iter.next();
				Map rules = (Map) groupRules.get(id);
				Group g = new Group(id, new Integer(0), id, "");
				printGroup(g, rules);
			}
			
			printFooter();
			
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TypeModeError e) {
			e.printStackTrace();
		} catch (TyrubaException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param group
	 * @param rules
	 */
	private static void printGroup(Group group, Map rules) {

		out.println("<h2><a name=\""+group.id+"\">"+group.label+"</a></h2>");
		
		out.println("<p>"+group.description+"</p>");
		
		out.println("<table cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");
		out.println("<tr>");
		out.println("<td class=\"TyrubaDocHeading\">Predicate</td><td class=\"TyrubaDocHeading\">Description</td>");
		out.println("</tr>");
		
		Iterator iter = rules.keySet().iterator();
		while (iter.hasNext()) {
			String rule = (String) iter.next();
			String doc = (String) rules.get(rule);
			printDoc(rule, doc);
		}
		
		out.println("</table>");
	}

	/**
	 * 
	 */
	private static void printHeader() throws IOException {
	
		if (header != null && header.exists()) {
			copyfile(header);
		}
		else {
			out.println("<html>");
			out.println("<head>");
			out.println("<title>TyRuBa Documentation</title>");
			out.println("<style>");
			out.println(".TyrubaDocHeading { background-color: #CCCCCC; font-weight: bold; }");
			out.println("</style>");
			out.println("</head>");
			out.println("<body>");	
		}		
	}

	/**
	 * @param rule
	 * @param doc
	 */
	private static void printDoc(String rule, String doc) {

		out.println("<tr>");
		out.println("<td class=\"TyrubaDocPredicate\">"+rule+"</td>");
		out.println("<td class=\"TyrubaDocDescription\">"+doc+"</td>");
		out.println("</tr>");
		
	}
	
	/**
	 * 
	 */
	private static void printFooter() throws IOException {
		
		if (footer != null && footer.exists()) {
			copyfile(footer);
		}
		else {
			out.println("</body>");
			out.println("</html>");
		}
	}

	private static void copyfile(File f) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(f));
		String line = reader.readLine();
		while (line != null) {
			out.println(line);
			line = reader.readLine();
		}
		reader.close();
	}

	
	private static void usage() {
		System.out.println("usage: TyrubaDoc <input file> <output file> [header] [footer]");
		System.out.println("\t<input file> = the tyruba rules file containing documentation");
		System.out.println("\t<output file> = the file to which the html is to be written");
		System.out.println("\t[header] = optional arg indicating file to use as the header for the output");
		System.out.println("\t[footer] = optional arg indicating file to use as the footer for the output");
	}
}

class Group implements Comparable {
	
	public String id;
	public Integer order;
	public String label;
	public String description;
	
	public Group(String id, Integer order, String label, String description) {
		this.id = id;
		this.order = order;
		this.label = label;
		this.description = description;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Group)) {
			return false;
		}
		Group o = (Group) obj;
		return this.id.equals(o.id)
			&& this.order.equals(o.order)
			&& this.label.equals(o.label)
			&& this.description.equals(o.description);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public int compareTo(Object o) {
		
		Group g = (Group) o;
		
		int i = this.order.compareTo(g.order);
		
		if (i != 0) {
			return i;
		}
		
		i = this.label.compareTo(g.label);
		if (i != 0) {
			return i;
		}
		
		i = this.id.compareTo(g.id);
		if (i != 0) {
			return i;
		}
		
		i = this.description.compareTo(g.description);
		return  i;
	}
}
