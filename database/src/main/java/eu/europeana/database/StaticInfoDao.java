/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.database;

import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.User;

import java.util.List;

/**
 * Handles all access to the information that doesn't really change very much over time.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia
 */

public interface StaticInfoDao {

    // ==== Partners

    List<Partner> getAllPartnerItems();
    List<Partner> fetchPartners();
    Partner savePartner(Partner partner);
    boolean removePartner(Long partnerId);

    // ==== Contributors

    List<Contributor> getAllContributorItems();
    List<Contributor> fetchContributors();
    Contributor saveContributor(Contributor contributor);
    boolean removeContributor(Long contributorId);

    // ==== Static pages

    StaticPage fetchStaticPage(Language language, String pageName);
    void setStaticPage(StaticPageType pageType, Language language, String content);
    List<StaticPage> getAllStaticPages();
    StaticPage fetchStaticPage(StaticPageType pageType, Language language);
    StaticPage saveStaticPage(Long staticPageId, String content);

    // ==== Translations

    List<MessageKey> getAllTranslationMessages();

    // === Carousel Items

    User removeCarouselItem(User user, Long savedItemId);
    User addCarouselItem(User user, CarouselItem carouselItem);
    CarouselItem addCarouselItem(User user, Long savedItem);
    User addCarouselItem(User user, SavedItem savedItem);
    Boolean removeCarouselItem(Long id);
    List<CarouselItem> fetchCarouselItems();
    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
    void removeFromCarousel(SavedItem savedItem);
    boolean addCarouselItem(SavedItem savedItem);

    // ==== Search Terms

    boolean addSearchTerm(Language language, String term);
    boolean addSearchTerm(SavedSearch savedSearch);
    SearchTerm addSearchTerm(Long savedSearchId);
    List<String> fetchSearchTerms(Language language);
    boolean removeSearchTerm(Language language, String term);
    List<SearchTerm> getAllSearchTerms();
    User removeSearchTerm(User user, Long savedSearchId);

    // ==== Editor picks

    User addEditorPick(User user, EditorPick editorPick);
    List<EditorPick> fetchEditorPicksItems();
    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
    void removeFromEditorPick(SavedSearch savedSearch);
}