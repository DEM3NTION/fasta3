import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to parse a DDL file and finally writes back the content to
 * the driver file FILE_NAME
 * 
 * @author crystalonix
 *
 */
public class DDLParser {

	private static final String WRITE = "write";
	// device pin
	private static final String DEVICE_TYPE = "gpio_";
	private static final String INPUT_MODE = "in";
	private static final String OUTPUT_MODE = "out";
	private static final String READ = "read";
	// name of the driver file name to be generated
	private static final String FILE_NAME = "pio.c";
	// input directive
	private static final String INCLUDE_INPUT = "#include <ddl_io_in.inc>";
	// output directive
	private static final String INCLUDE_OUTPUT = "#include <ddl_io_out.inc>";
	// this file is used as backbone to generate the driver file
	private static final String BACKBONE_FILE_NAME = "pio_backbone";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<device> devices = new ArrayList<>();
		try {
			File inputFile = new File("DDL.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("device");
			// System.out.println("----------------------------");
			for (int i = 0; i < nList.getLength(); i++) {
				Node n = nList.item(i);
				device dev = new device();
				dev.setName(n.getNodeName());
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					// System.out.println("hello");
					Element devElem = (Element) n;
					NodeList pns = devElem.getElementsByTagName("pin");
					for (int j = 0; j < pns.getLength(); j++) {
						pin p = new pin();
						p.setId(((Element) (pns.item(j))).getElementsByTagName("id").item(0).getTextContent());
						p.setIndex(Integer.parseInt(
								((Element) (pns.item(j))).getElementsByTagName("index").item(0).getTextContent()));
						p.setMode(((Element) (pns.item(j))).getElementsByTagName("mode").item(0).getTextContent());
						p.setLevel(((Element) (pns.item(j))).getElementsByTagName("level").item(0).getTextContent());
						dev.addPin(p);
					}
				}
				devices.add(dev);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error occurred" + e);
		}
		StringBuilder outWrite = new StringBuilder();
		StringBuilder inRead = new StringBuilder();
		for (int i = 0; i < devices.size(); i++) {
			List<pin> ps = devices.get(i).pins;
			for (int j = 0; j < ps.size(); j++) {
				if (ps.get(j).getMode().equals(OUTPUT_MODE)) {
					outWrite.append("case " + ps.get(j).getIndex() + ":\n");
					outWrite.append(DEVICE_TYPE + WRITE + "(" + ps.get(j).index + ",data" + ")\n");
				} else if (ps.get(j).getMode().equals(INPUT_MODE)) {
					inRead.append("case " + ps.get(j).getIndex() + ":\n");
					inRead.append(DEVICE_TYPE + READ + "(" + ps.get(j).index + ",data" + ")\n");
				}
			}
		}
		inRead.deleteCharAt(inRead.length()-1);
		outWrite.deleteCharAt(outWrite.length()-1);
		BufferedReader readFile = null;
		try {
			readFile = new BufferedReader(new FileReader(BACKBONE_FILE_NAME));
		} catch (FileNotFoundException e2) {
			System.out.println("Error opeing file:" + FILE_NAME);
		}
		StringBuilder fileReader = new StringBuilder();
		String temp = null;

		try {
			while ((temp = readFile.readLine()) != null) {
				if (temp.contains(INCLUDE_INPUT)) {
					String str = temp.replace(INCLUDE_INPUT, "");
					fileReader.append(str);
					fileReader.append(inRead.toString().replaceAll("\n",'\n'+str));
					fileReader.append("\n");
				} else if (temp.contains(INCLUDE_OUTPUT)) {
					String str = temp.replace(INCLUDE_OUTPUT, "");
					fileReader.append(str);
					fileReader.append(outWrite.toString().replaceAll("\n",'\n'+str));
					fileReader.append("\n");
				} else {
					fileReader.append(temp).append("\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problem reading from file:" + FILE_NAME);
		} finally {
			try {
				readFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error trying to close file reader:" + FILE_NAME);
			}
		}

		BufferedWriter outputWriter = null;

		// creating the output file if not already exists
		File file = new File(FILE_NAME);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Output file could not be created");
				return;
			}
		}
		/**
		 * Preparing the output stream
		 */
		FileWriter fw;
		try {
			fw = new FileWriter(file);
		} catch (IOException e1) {
			System.out.println("FileWriter could not be opened:" + e1);
			return;
		}
		outputWriter = new BufferedWriter(fw);
		try {
			outputWriter.write(fileReader.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				outputWriter.close();
			} catch (IOException e) {
				System.out.println("Error trying to close the file writer:" + FILE_NAME);
			}
		}
	}
}

class device {
	String name;
	List<pin> pins = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addPin(pin pn) {
		pins.add(pn);
	}
}

class pin {
	String id;
	int index;
	String mode;
	String level;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
