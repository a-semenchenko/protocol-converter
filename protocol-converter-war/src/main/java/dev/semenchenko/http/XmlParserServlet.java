package dev.semenchenko.http;

import dev.semenchenko.jaxb.Envelope;
import dev.semenchenko.jaxb.JaxbMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
public class XmlParserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(XmlParserServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JaxbMarshaller marshaller = new JaxbMarshaller();
        String xml = req.getParameter("xml");
        StringReader reader = new StringReader(xml);
        try {
            Envelope envelope = (Envelope) marshaller.unmarshallFromXML(reader, Envelope.class);
            logger.debug(envelope.toString());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
