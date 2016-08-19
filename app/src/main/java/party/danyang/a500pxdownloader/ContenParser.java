package party.danyang.a500pxdownloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by dream on 16-8-19.
 */
public class ContenParser {
    public static String parser(String html) {
        Document doc = Jsoup.parse(html);
        Elements metas = doc.select("meta");
        for (Element meta : metas) {
            if (meta.attr("property").equals("og:image")) {
                return meta.attr("content");
            }
        }
        return null;
    }
}
