package dev.semenchenko.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
public class JaxbMarshaller {

    public Object unmarshallFromXML(StringReader reader, Class clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(reader);
    }
}
