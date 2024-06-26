package newsonthego.storage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ArticleScraper {


    /**
     * Removes special characters from the input text.
     *
     * @param text The input text.
     * @return The text with special characters removed.
     */
    private static String removeSpecialCharacters(String text) {
        // Use a regular expression to remove all special characters except for letters and digits
        return text.replaceAll("[^a-zA-Z0-9\\s]", "");
    }


    /**
     * Scrapes an article from the given URL and saves its details to a file.
     *
     * @param url             The URL of the article.
     * @param outputFolderPath The path to the folder where the output file will be saved.
     */
    public static void scrapeArticle(String url, String outputFolderPath) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Extract theme from navigation menu (assuming it's in a specific element)
            String theme = extractTheme(doc);

            // Extract source of the article
            String source = extractSource(doc);

            // Extract published date in multiple formats
            String publishedDate = extractPublishedDate(doc);

            // Extract author name
            String author = extractAuthor(doc);

            // Extract abstract
            Element abstractElement = doc.selectFirst("meta[name=description]");
            String abstractText = (abstractElement != null) ? abstractElement.attr("content") : "Abstract not found";
            abstractText = removeSpecialCharacters(abstractText);

            // Extract headlines
            String headline = doc.title();
            headline = removeSpecialCharacters(headline);

            // Generate the output file path within the data folder
            String outputFilePath = outputFolderPath + File.separator + "testArticleScraper.txt";

            // Write the extracted information to the output file in append mode
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
                writer.write(headline + ";" + author + ";" + publishedDate + ";"
                        + source + ";" + url + ";" + abstractText + ";" + theme + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the source of the article.
     *
     * @param doc The document object representing the HTML content of the article.
     * @return The source of the article.
     */
    private static String extractSource(Document doc) {
        // Try to extract source from the og:site_name property
        Element sourceElement = doc.selectFirst("meta[property=og:site_name]");
        if (sourceElement != null) {
            return sourceElement.attr("content");
        }

        // If og:site_name is not found, try to extract source from al:android:app_name property
        Element fallbackSourceElement = doc.selectFirst("meta[property=al:android:app_name]");
        if (fallbackSourceElement != null) {
            return fallbackSourceElement.attr("content");
        }

        // If both properties are not found, return "Unknown"
        return "Unknown";
    }

    /**
     * Extracts the theme of the article.
     *
     * @param doc The document object representing the HTML content of the article.
     * @return The theme of the article.
     */
    private static String extractTheme(Document doc) {
        // Try to extract theme from Open Graph metadata (og:category)
        Element metaTag = doc.selectFirst("meta[property=og:category]");
        if (metaTag != null) {
            return standardizeTheme(metaTag.attr("content"));
        }

        // If theme not found in Open Graph metadata, try another format
        Element themeElement = doc.selectFirst("meta[name=categories]");
        if (themeElement != null) {
            return standardizeTheme(themeElement.attr("content"));
        }

        // If theme not found in the second format, try "theme" metadata
        Element themeMetaElement = doc.selectFirst("meta[name=theme]");
        if (themeMetaElement != null) {
            return standardizeTheme(themeMetaElement.attr("content"));
        }

        // If theme not found in the third format, try "article:section" metadata
        Element sectionElement = doc.selectFirst("meta[property=article:section]");
        String theme = (sectionElement != null) ? sectionElement.attr("content") : "Theme not found";
        return standardizeTheme(theme);
    }

    /**
     * Standardizes the theme/topic of the article.
     *
     * @param theme The extracted theme/topic.
     * @return The standardized theme/topic.
     */
    private static String standardizeTheme(String theme) {

        if (theme.equalsIgnoreCase("tech") || theme.equalsIgnoreCase("technology")) {
            return "Technology";
        } else if (theme.equalsIgnoreCase("world") || theme.equalsIgnoreCase("international") ||
                theme.equalsIgnoreCase("world news") || theme.equalsIgnoreCase("asia") ||
                theme.equalsIgnoreCase("asian news") || theme.equalsIgnoreCase("singapore") ||
                theme.equalsIgnoreCase("singapore news") || theme.equalsIgnoreCase("india") ||
                theme.equalsIgnoreCase("indian news") || theme.equalsIgnoreCase("china") ||
                theme.equalsIgnoreCase("chinese news") || theme.equalsIgnoreCase("americas") ||
                theme.equalsIgnoreCase("american news") || theme.equalsIgnoreCase("north america") ||
                theme.equalsIgnoreCase("south america") || theme.equalsIgnoreCase("europe") ||
                theme.equalsIgnoreCase("european news") || theme.equalsIgnoreCase("uk") ||
                theme.equalsIgnoreCase("united kingdom") || theme.equalsIgnoreCase("britain") ||
                theme.equalsIgnoreCase("british news") || theme.equalsIgnoreCase("uk news")) {
            return "World";
        } else if (theme.equalsIgnoreCase("politics") || theme.equalsIgnoreCase("political")) {
            return "Politics";
        } else if (theme.equalsIgnoreCase("business") || theme.equalsIgnoreCase("finance") ||
                theme.equalsIgnoreCase("economy")) {
            return "Business";
        } else if (theme.equalsIgnoreCase("health") || theme.equalsIgnoreCase("medical")) {
            return "Health";
        } else if (theme.equalsIgnoreCase("sports") || theme.equalsIgnoreCase("sport")) {
            return "Sports";
        } else if (theme.equalsIgnoreCase("entertainment") || theme.equalsIgnoreCase("celebrity")) {
            return "Entertainment";
        } else {
            return "Others";
        }
    }

    /**
     * Extracts the published date of the article.
     *
     * @param doc The document object representing the HTML content of the article.
     * @return The published date of the article.
     */
    private static String extractPublishedDate(Document doc) {
        // Try to extract published date in multiple formats
        Element dateElement = doc.selectFirst("meta[property=article:published_time]");
        if (dateElement != null) {
            return normalizeDate(dateElement.attr("content"));
        }

        // If published date not found in the first format, try another format
        Element anotherDateElement = doc.selectFirst("meta[name=cXenseParse:recs:publishtime]");
        if (anotherDateElement != null) {
            return normalizeDate(anotherDateElement.attr("content"));
        }

        // If published date not found in the second format, try "article:published" metadata
        Element publishedElement = doc.selectFirst("meta[property=article:published]");
        if (publishedElement != null) {
            return normalizeDate(publishedElement.attr("content"));
        }

        return "Published date not found";
    }

    /**
     * Normalizes the date format.
     *
     * @param dateString The date string to be normalized.
     * @return The normalized date string.
     */
    private static String normalizeDate(String dateString) {
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'", // Example: 2024-03-10T12:30:45Z
            "yyyy-MM-dd'T'HH:mm:ss",    // Example: 2024-03-10T12:30:45
            "yyyy-MM-dd",               // Example: 2024-03-10
            "yyyy/MM/dd",               // Example: 2024/03/10
            "MM/dd/yyyy",               // Example: 03/10/2024
            "dd-MM-yyyy",               // Example: 10-03-2024
            "dd/MM/yyyy",               // Example: 10/03/2024
            "MMM dd, yyyy",             // Example: Mar 10, 2024
            "MMMM dd, yyyy"             // Example: March 10, 2024
        };

        for (String format : formats) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
            Date date = null;
            boolean parseSuccess = true;

            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                parseSuccess = false;
            }

            if (parseSuccess && date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                return outputFormat.format(date);
            }
        }
        return "Invalid date format";
    }

    /**
     * Removes entire words with special characters from the input text.
     *
     * @param text The input text.
     * @return The text with entire words containing special characters removed.
     */
    private static String removeWordsWithSpecialChars(String text) {
        // Define a regular expression pattern to match words with special characters
        // The pattern matches any word that contains a character that is not a letter, digit, or space
        String pattern = "\\b\\S*[^a-zA-Z0-9\\s]+\\S*\\b";

        // Replace words with special characters with an empty string
        return text.replaceAll(pattern, "");
    }

    /**
     * Extracts the author of the article.
     *
     * @param doc The document object representing the HTML content of the article.
     * @return The author of the article.
     */

    private static String extractAuthor(Document doc) {
        // Try to extract author name from meta tag with name="author"
        Element authorElement = doc.selectFirst("meta[name=author]");
        if (authorElement != null) {
            String authorName = authorElement.attr("content");
            if (!authorName.isEmpty()) {
                // Replace commas with "and" if the author has multiple names
                authorName = authorName.replace(",", " and");
                // Apply function to remove entire words with special characters
                authorName = removeWordsWithSpecialChars(authorName);
                return authorName;
            }
        }

        // If author not found in the meta tag, try another format
        Element cXenseAuthorElement = doc.selectFirst("meta[name=cXenseParse:author]");
        if (cXenseAuthorElement != null) {
            String authorName = cXenseAuthorElement.attr("content");
            if (!authorName.isEmpty()) {
                // Replace commas with "and" if the author has multiple names
                authorName = authorName.replace(",", " and");
                // Apply function to remove entire words with special characters
                authorName = removeWordsWithSpecialChars(authorName);
                return authorName;
            }
        }

        // If author not found in both formats, return "Unknown"
        return "Unknown";
    }

}
