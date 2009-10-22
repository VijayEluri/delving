/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
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

package eu.europeana.dashboard.server;

import eu.europeana.dashboard.client.dto.*;
import eu.europeana.database.domain.*;
import eu.europeana.query.DocType;

/**
 * Do all the conversions to data transfer objects from domain objects
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DataTransfer {

    public static EuropeanaCollectionX convert(EuropeanaCollection collection) {
        return new EuropeanaCollectionX(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getFileName(),
                collection.getCollectionLastModified(),
                collection.getFileUserName(),
                collection.getFileState().toString(),
                collection.getCacheState().toString(),
                collection.getCollectionState().toString(),
                collection.getTotalRecords(),
                collection.getTotalObjects(),
                collection.getTotalOrphans(),
                collection.getImportError()
        );
    }

    public static CarouselItemX convert(CarouselItem item) {
        String typeString = (item.getType() == null) ? DocType.UNKNOWN.toString() : item.getType().toString();
        return new CarouselItemX(
                item.getId(),
                item.getEuropeanaUri(),
                item.getTitle(),
                item.getThumbnail(),
                item.getCreator(),
                item.getYear(),
                item.getProvider(),
                item.getLanguage().getName(),
                CarouselItemX.DocTypeX.valueOf(typeString)
        );
    }

    public static EuropeanaIdX convert(EuropeanaId id) {
        return new EuropeanaIdX(
                id.getId(),
                id.getTimesViewed(),
                id.getCreated(),
                id.getLastViewed(),
                id.getLastModified(),
                id.getEuropeanaUri(),
                id.getBoostFactor()
        );
    }

    public static SavedSearchX convert(SavedSearch s) {
        return new SavedSearchX(
                s.getId(),
                s.getQueryString(),
                convert(s.getLanguage())
        );
    }

    public static LanguageX convert(Language language) {
        return new LanguageX(
                language.getCode(),
                language.getName()
        );
    }

    public static PartnerX convert(Partner partner) {
        return new PartnerX(
                partner.getId(),
                partner.getName(),
                partner.getSector().toString(),
                partner.getUrl()
        );
    }

    public static Partner convert(PartnerX partnerX) {
        Partner partner = new Partner(partnerX.getId());
        partner.setName(partnerX.getName());
        partner.setSector(PartnerSector.valueOf(partnerX.getSector()));
        partner.setUrl(partnerX.getUrl());
        return partner;
    }

    public static ContributorX convert(Contributor contributor) {
        return new ContributorX(
                contributor.getId(),
                convert(contributor.getCountry()),
                contributor.getProviderId(),
                contributor.getOriginalName(),
                contributor.getEnglishName(),
                contributor.getAcronym(),
                contributor.getNumberOfPartners(),
                contributor.getUrl()
        );
    }

    public static Contributor convert(ContributorX contributorX) {
        Contributor contributor = new Contributor(contributorX.getId());
        contributor.setCountry(Country.valueOf(contributorX.getCountry().getCode()));
        contributor.setProviderId(contributorX.getProviderId());
        contributor.setOriginalName(contributorX.getOriginalName());
        contributor.setEnglishName(contributorX.getEnglishName());
        contributor.setAcronym(contributorX.getAcronym());
        contributor.setNumberOfPartners(contributorX.getNumberOfPartners());
        contributor.setUrl(contributorX.getUrl());
        return contributor;
    }

    public static CountryX convert(Country country) {
        return new CountryX(country.toString(), country.getEnglishName());
    }

    public static UserX convert(User user) {
        return new UserX(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getLanguages(),
                user.getProjectId(),
                user.getProviderId(),
                user.isNewsletter(),
                user.getRegistrationDate(),
                user.getLastLogin(),
                RoleX.valueOf(user.getRole().toString()),
                user.isEnabled()
        );
    }

    public static StaticPageX convert(StaticPage page) {
        return new StaticPageX(
                page.getId(),
                page.getPageType().toString(),
                convert(page.getLanguage()),
                page.getContent()
        );
    }

    public static TranslationX convert(Translation translation) {
        return new TranslationX(
                convert(translation.getLanguage()),
                translation.getValue()
        );
    }

    public static DashboardLogX convert(DashboardLog log) {
        return new DashboardLogX(
                log.getId(),
                log.getWho(),
                log.getTime(),
                log.getWhat()
        );
    }

}