package ranalyzer.utility;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ranalyzer.model.UmlClass;
import ranalyzer.model.ClassDiagram;
import ranalyzer.model.UmlRelation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlToUmlParser {

    @Getter
    private ClassDiagram classDiagram = new ClassDiagram();

    public void parse(Document document) throws UnsupportedMediaException {
        Map<String, String> idName = new HashMap<>();
        List<UmlRelation> relations = classDiagram.getRelations();
        List<UmlClass> classes = classDiagram.getClasses();
        Element e = document.getDocumentElement();
        NodeList list = e.getElementsByTagName("packagedElement");
        for (int i = 0; i < list.getLength(); i++) {
            e = (Element) list.item(i);
            String id = e.getAttribute("xmi:id"), type = e.getAttribute("xsi:type");
            type = type.replaceFirst("uml:", "");
            switch (type) {
                case "Class": {
                    UmlClass umlClass = new UmlClass();
                    String name = e.getAttribute("name");
                    idName.put(id, name);
                    umlClass.setName(name);
                    addAttributesAndOperation(e, umlClass);
                    classes.add(umlClass);
                    if (e.hasAttribute("interfaceRealization")) {
                        NodeList realizations = e.getElementsByTagName("interfaceRealization");
                        for (int j = 0; j < realizations.getLength(); j++) {
                            Element e2 = (Element) realizations.item(j);
                            UmlRelation umlRelation = new UmlRelation();
                            umlRelation.setFromTo(idName.get(e2.getAttribute("client")), idName.get(e2.getAttribute("supplier")));
                        }
                    }
                    break;
                }
                case "Enumeration": {
                    UmlClass umlClass = new UmlClass();
                    String name = e.getAttribute("name");
                    idName.put(id, name);
                    umlClass.setName(name);
                    NodeList literals = e.getElementsByTagName("ownedLiteral");
                    for (int j = 0; j < literals.getLength(); j++) {
                        Element e2 = (Element) literals.item(j);
                        umlClass.getAttributes().add(e2.getAttribute("name"));
                    }
                    classes.add(umlClass);
                    break;
                }
                case "Interface": {
                    UmlClass umlClass = new UmlClass();
                    String name = e.getAttribute("name");
                    idName.put(id, name);
                    umlClass.setName(name);
                    addAttributesAndOperation(e, umlClass);
                    classes.add(umlClass);
                    break;
                }
                case "DataType": {
                    UmlClass umlClass = new UmlClass();
                    String name = e.getAttribute("name");
                    idName.put(id, name);
                    umlClass.setName(name);
                    classes.add(umlClass);
                    break;
                }
                case "Association": {
                    // association, composition, aggregation will enter here
                    UmlRelation umlRelation = new UmlRelation();
                    umlRelation.setType("Association");
                    NodeList endpoint = e.getElementsByTagName("ownedEnd");
                    Element eTo = (Element) endpoint.item(0), eFrom = (Element) endpoint.item(1);
                    umlRelation.setFromTo(idName.get(eFrom.getAttribute("type")), idName.get(eTo.getAttribute("type")));
                    relations.add(umlRelation);
                    break;
                }
                case "Dependency": {
                    UmlRelation umlRelation = new UmlRelation();
                    umlRelation.setType("Dependency");
                    umlRelation.setFromTo(idName.get(e.getAttribute("client")), idName.get(e.getAttribute("supplier")));
                    relations.add(umlRelation);
                    break;
                }
                case "Usage": {
                    UmlRelation umlRelation = new UmlRelation();
                    umlRelation.setType("Dependency");
                    umlRelation.setFromTo(idName.get(e.getAttribute("client")), idName.get(e.getAttribute("supplier")));
                    relations.add(umlRelation);
                    break;
                }
                default: {
                    throw new UnsupportedMediaException("XMI File not Supported");
                }
            }
        }
    }

    private void addAttributesAndOperation(Element e, final UmlClass umlClass) {
        NodeList attributes = e.getElementsByTagName("ownedAttribute"), methods = e.getElementsByTagName("ownedOperation");
        for (int j = 0; j < attributes.getLength(); j++) {
            Element e2 = (Element) attributes.item(j);
            umlClass.getAttributes().add(e2.getAttribute("name"));
        }
        for (int j = 0; j < methods.getLength(); j++) {
            Element e2 = (Element) methods.item(j);
            umlClass.getMethods().add(e2.getAttribute("name"));
        }
    }
}
