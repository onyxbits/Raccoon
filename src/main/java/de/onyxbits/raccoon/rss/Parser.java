package de.onyxbits.raccoon.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

public class Parser {
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String CHANNEL = "channel";
	static final String LANGUAGE = "language";
	static final String COPYRIGHT = "copyright";
	static final String LINK = "link";
	static final String AUTHOR = "author";
	static final String ITEM = "item";
	static final String PUB_DATE = "pubDate";
	static final String GUID = "guid";

	final URL url;

	public Parser(String feedUrl) {
		try {
			this.url = new URL(feedUrl);
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public Feed readFeed() {
    Feed feed = null;
    try {
      boolean isFeedHeader = true;
      // Set header values intial to the empty string
      String description = "";
      String title = "";
      String link = "";
      String language = "";
      String copyright = "";
      String author = "";
      String pubdate = "";
      String guid = "";

      // First create a new XMLInputFactory
      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      // Setup a new eventReader
      InputStream in = read();
      XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
      // read the XML document
      while (eventReader.hasNext()) {
        XMLEvent event = eventReader.nextEvent();
        if (event.isStartElement()) {
          String localPart = event.asStartElement().getName()
              .getLocalPart();
          if (localPart.equals(ITEM)) {
            if (isFeedHeader) {
              isFeedHeader = false;
              feed = new Feed(title, link, description, language,
                  copyright, pubdate);
            }
            event = eventReader.nextEvent();
          }
          if (localPart.equals(TITLE)) {
            title = getCharacterData(event, eventReader);
          }
          if (localPart.equals(DESCRIPTION)) {
            description = getCharacterData(event, eventReader);
          }
          if (localPart.equals(LINK)) {
            link = getCharacterData(event, eventReader);
          }
          if (localPart.equals(GUID)) {
            guid = getCharacterData(event, eventReader);
          }
          if (localPart.equals(LANGUAGE)) {
            language = getCharacterData(event, eventReader);
          }
          if (localPart.equals(AUTHOR)) {
            author = getCharacterData(event, eventReader);
          }
          if (localPart.equals(PUB_DATE)) {
            pubdate = getCharacterData(event, eventReader);
          }
          if (localPart.equals(COPYRIGHT)) {
            copyright = getCharacterData(event, eventReader);
          }
        } else if (event.isEndElement()) {
          if (event.asEndElement().getName().getLocalPart() == (ITEM)) {
            Message message = new Message();
            message.setAuthor(author);
            message.setDescription(description);
            message.setGuid(guid);
            message.setLink(link);
            message.setTitle(title);
            feed.getMessages().add(0,message);
            event = eventReader.nextEvent();
            continue;
          }
        }
      }
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    return feed;
  }

	private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
			throws XMLStreamException {
		String result = "";
		event = eventReader.nextEvent();
		if (event instanceof Characters) {
			result = event.asCharacters().getData();
		}
		return result;
	}

	private InputStream read() {
		try {
			return url.openStream();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}