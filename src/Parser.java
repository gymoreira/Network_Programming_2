import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
/*
Example json extracted from osm
[{
                "name": "Rodoviária de Feira de Santana",
                "type": "bus_station",
                "point": {
                        "lat": -12.2590039,
                        "lgt": -38.9585199
                }
        },
        {
                "name": null,
                "type": "parking",
                "point": {
                        "lat": -12.2578876,
                        "lgt": -38.9579019
                }
        }
]
 */
/*
Activity 2: XML parser and json file generation.
You must extract the XML file space objects,
the objects must have a type (amenity),
a name (optional) and a point (latitude and longitude)

To validate the json produced, you can use the:
https://jsonlint.com
 */
public class Parser {

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(new FileReader("FeiraDeSantana.osm"));

        HashMap<Long, Point> nodes = new HashMap<>();
        List<SpatialObject> list = new LinkedList<>();

        SpatialObject soAtual = null;

        while (parser.hasNext()) {
            int event = parser.next();

            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    switch (parser.getName().getLocalPart()) {
                        case "node":
                            Double lat = getAttValueDouble(parser, "lat");
                            Double lgt = getAttValueDouble(parser, "lon");
                            Long id = getAttValueLong(parser, "id");

                            if (id!=null && lat!=null && lgt!=null) {
                                Point p = new Point(lat, lgt);
                                nodes.put(id, p);
                                soAtual = new SpatialObject(p);
                            }
                            break;
                        case "way":
                            soAtual = new SpatialObject();
                            break;
                        case "nd":
                            Long ref = getAttValueLong(parser, "ref");
                            Point ponto = nodes.get(ref);
                            soAtual.setPoint(ponto);
                            break;
                        case "tag":
                            if (getAttValue(parser, "k").equals("amenity")){
                                soAtual.setType(getAttValue(parser, "v"));
                                list.add(soAtual);
                            } else if (getAttValue(parser, "k").equals("name")){
                                if (soAtual != null && soAtual.getType() != null){
                                    soAtual.setName(getAttValue(parser, "v"));
                                    list.remove(list.size() - 1);
                                    list.add(soAtual);
                                }
                            }
                            break;
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (parser.getName().getLocalPart().equals("node") || parser.getName().getLocalPart().equals("way")) {
                        soAtual = null;
                    }
                    break;
            }
        }
//        System.out.println(list.size()+" objects found!");
        System.out.println(list.toString());
    }

    private static Long getAttValueLong(XMLStreamReader parser, String att) {
        String value = getAttValue(parser, att);
        return value == null ? null : Long.parseLong(value);
    }

    private static Double getAttValueDouble(XMLStreamReader parser, String att) {
        String value = getAttValue(parser, att);
        return value == null ? null : Double.parseDouble(value);
    }

    private static String getAttValue(XMLStreamReader parser, String att) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeName(i).getLocalPart().equals(att)) {
                return parser.getAttributeValue(i);
            }
        }
        return null;
    }
}
