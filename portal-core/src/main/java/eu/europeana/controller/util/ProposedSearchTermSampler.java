package eu.europeana.controller.util;

import eu.europeana.database.SearchTermDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.SearchTerm;

import java.util.*;

/**
 * Pick some objects from serch terms
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ProposedSearchTermSampler {
    private SearchTermDao searchTermDao;
    private int displayCount = 3;
    private Map<Language, List<SearchTerm>> cache = new HashMap<Language, List<SearchTerm>>();

    public void setSearchTermDao(SearchTermDao searchTermDao) {
        this.searchTermDao = searchTermDao;
    }

    public void setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
    }

    public List<SearchTerm> pickRandomItems(Language language) {
        List<SearchTerm> items = cache.get(language);
        if (items == null) {
            items = cache.get(Language.EN);
            if (items == null) {
                return Collections.emptyList();
            }
        }
        List<SearchTerm> selection = new ArrayList<SearchTerm>(items);
        while (selection.size() > displayCount) {
            int index = (int)(Math.random()*selection.size());
            selection.remove(index);
        }
        return selection;
    }

    public void refresh() {
        cache = getData();
    }

    private Map<Language, List<SearchTerm>> getData() {
        List<SearchTerm> allSearchTerms = searchTermDao.getAllSearchTerms();
        Map<Language, List<SearchTerm>> data = new HashMap<Language, List<SearchTerm>>();
        for (SearchTerm searchTerm : allSearchTerms) {
            List<SearchTerm> searchTerms = data.get(searchTerm.getLanguage());
            if (searchTerms == null) {
                searchTerms = new ArrayList<SearchTerm>();
                data.put(searchTerm.getLanguage(), searchTerms);
            }
            searchTerms.add(searchTerm);
        }
        return data;
    }
}