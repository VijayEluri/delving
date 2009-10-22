package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;
import eu.europeana.dashboard.client.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the client side of the RPC connection needed by the dashboard
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardService extends RemoteService {

    UserX login(String email, String password);
    void setUserRole(Long userId, RoleX role);
    List<UserX> fetchUsers(String pattern);
    void setUserEnabled(Long userId, boolean enabled);
    void setUserLanguages(Long userId, String languages);
    void setUserProjectId(Long userId, String projectId);
    void setUserProviderId(Long userId, String providerId);
    List<SavedItemX> fetchSavedItems(Long userId);
    void removeUser(Long userId);

    List<EuropeanaCollectionX> fetchCollections();
    List<EuropeanaCollectionX> fetchCollections(String prefix);
    EuropeanaCollectionX fetchCollection(String name, boolean create);
    EuropeanaCollectionX updateCollection(EuropeanaCollectionX collection);
    List<QueueEntryX> fetchQueueEntries();
    EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection);

    List<ImportFile> fetchImportFiles(boolean normalized);
    ImportFile commenceValidate(ImportFile file, Long collectionId);
    ImportFile commenceImport(ImportFile file, Long collectionId, boolean normalized);
    ImportFile abortImport(ImportFile file, boolean normalized);
    ImportFile checkImportFileStatus(String fileName, boolean normalized);

    List<String> fetchMessageKeys();
    List<LanguageX> fetchLanguages();
    Map<String,List<TranslationX>> fetchTranslations(Set<String> languageCodes);
    TranslationX setTranslation(String key, String language, String value);

    String fetchCacheUrl();
    List<CarouselItemX> fetchCarouselItems();
    CarouselItemX createCarouselItem(SavedItemX savedItemX);
    boolean removeCarouselItem(CarouselItemX item);

    boolean addSearchTerm(String language, String term);
    List<String> fetchSearchTerms(String language);
    boolean removeSearchTerm(String language, String term);

    List<String> getObjectOrphans();
    boolean deleteObjectOrphan(String uri);
    void deleteAllOrphans();

    EuropeanaIdX fetchEuropeanaId(String uri);
    EuropeanaIdX updateEuropeanaId(EuropeanaIdX europeanaId);

    List<SavedSearchX> fetchSavedSearches(Long id);

    List<String> fetchPartnerSectors();
    List<PartnerX> fetchPartners();
    List<CountryX> fetchCountries();
    List<ContributorX> fetchContributors();
    PartnerX savePartner(PartnerX partner);
    ContributorX saveContributor(ContributorX contributor);

    boolean removePartner(Long partnerId);
    boolean removeContributor(Long contributorId);

    List<String> fetchStaticPageTypes();
    StaticPageX fetchStaticPage(String pageType, LanguageX language);
    StaticPageX saveStaticPage(Long staticPageId, String content);

    void removeMessageKey(String key);
    void addMessageKey(String key);

    List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize);
    List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize);

    void disableAllCollections ();
    void enableAllCollections ();
}
