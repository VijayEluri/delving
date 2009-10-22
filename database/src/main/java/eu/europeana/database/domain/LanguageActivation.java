package eu.europeana.database.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * @author vitali
 */
@Entity
public class LanguageActivation {

    @Id
    @Column(length=3)
    @Enumerated(EnumType.STRING)
    Language language;

    @Column
    boolean active;

    public LanguageActivation(Language language, boolean active) {
        this.language = language;
        this.active = active;
    }

    public LanguageActivation() {
    }

    public Language getLanguage() {
        return language;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}