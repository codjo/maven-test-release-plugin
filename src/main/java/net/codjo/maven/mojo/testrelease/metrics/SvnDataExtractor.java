package net.codjo.maven.mojo.testrelease.metrics;
/**
 *
 */
class SvnDataExtractor {
    protected static final String INVALID_ENTRIES_FORMAT
          = "Format du fichier SVN invalid. Vous utilisez probablement une version SVN trop récente";


    public SvnData extract(String entriesFileContent) {
        SvnData data = new SvnData();
        if (isSvn15Format(entriesFileContent)) {
            String[] rows = entriesFileContent.split("\n");
            if (rows.length < 5) {
                throw new IllegalArgumentException(INVALID_ENTRIES_FORMAT);
            }
            data.setRevision(rows[3].trim());
            data.setUrl(rows[4].trim());
        }
        else {
            data.setRevision(extractValue(entriesFileContent, "revision=\"", "\""));
            data.setUrl(extractValue(entriesFileContent, "url=\"", "\""));
        }
        return data;
    }


    private boolean isSvn15Format(String entriesFileContent) {
        return !entriesFileContent.contains("<wc-entries");
    }


    private String extractValue(String entriesFileContent, String fromTag, String endTag) {
        int start = entriesFileContent.indexOf(fromTag);
        if (start == -1) {
            throw new IllegalArgumentException(INVALID_ENTRIES_FORMAT);
        }
        int end = entriesFileContent.indexOf(endTag, start + fromTag.length());
        return entriesFileContent.substring(start + fromTag.length(), end);
    }
}
