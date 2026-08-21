package com.example.Database;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The same rows as XML:
 *
 * <pre>
 * &lt;rows&gt;&lt;row&gt;&lt;cell&gt;Capital of France?&lt;/cell&gt;&lt;/row&gt;&lt;/rows&gt;
 * </pre>
 *
 * <p>The parser has DTDs and external entities switched off. This reads files a user uploaded,
 * and an XML parser at its defaults will happily fetch a URL or read a local file that the
 * document tells it to.</p>
 */
public class XMLAdapter implements Database {

    private static final Logger logger = Logger.getLogger(XMLAdapter.class.getName());

    private static final String ROOT = "rows";
    private static final String ROW = "row";
    private static final String CELL = "cell";

    private final DataDirectory dataDirectory;

    public XMLAdapter(DataDirectory dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>();
        Path path = dataDirectory.resolve(fileName);
        if (!Files.exists(path)) {
            logger.warning("No such data file, returning nothing: " + path);
            return data;
        }
        try {
            Document document = documentBuilder().parse(path.toFile());
            NodeList rows = document.getElementsByTagName(ROW);
            for (int i = 0; i < rows.getLength(); i++) {
                data.add(cellsOf(rows.item(i)));
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Error reading " + path, e);
            return new ArrayList<>();
        }
        return data;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        Path path = dataDirectory.resolve(fileName);
        try (OutputStream out = Files.newOutputStream(path)) {
            Document document = documentBuilder().newDocument();
            Element root = document.createElement(ROOT);
            document.appendChild(root);
            for (String[] row : data) {
                Element rowElement = document.createElement(ROW);
                for (String cell : row) {
                    Element cellElement = document.createElement(CELL);
                    cellElement.setTextContent(cell);
                    rowElement.appendChild(cellElement);
                }
                root.appendChild(rowElement);
            }
            transformer().transform(new DOMSource(document), new StreamResult(out));
        } catch (IOException | TransformerException | ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Error writing " + path, e);
        }
    }

    private static String[] cellsOf(Node row) {
        NodeList children = row.getChildNodes();
        ArrayList<String> cells = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && CELL.equals(child.getNodeName())) {
                cells.add(child.getTextContent());
            }
        }
        return cells.toArray(new String[0]);
    }

    private static DocumentBuilder documentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private static Transformer transformer() throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        return transformer;
    }
}
