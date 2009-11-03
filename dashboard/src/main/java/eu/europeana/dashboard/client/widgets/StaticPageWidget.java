package eu.europeana.dashboard.client.widgets;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.LanguageX;
import eu.europeana.dashboard.client.dto.StaticPageX;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import java.util.List;

/**
 * Provide for the editing of static page content using a rich text editor
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class StaticPageWidget extends DashboardWidget {
    private List<LanguageX> languages;
    private ListBox pageBox = new ListBox();
    private ListBox languageBox = new ListBox();
    private TextArea textArea = new TextArea();
    private HTML preview = new HTML();
    private boolean changed;
    private Button submit = new Button(world.messages().submit());
    private Button revert = new Button(world.messages().revert());
    private StaticPageX page;

    public StaticPageWidget(World world) {
        super(world);
    }

    protected Widget createWidget() {
        pageBox.addItem(world.messages().selectPage());
        world.service().fetchStaticPageTypes(new Reply<List<String>>() {
            public void onSuccess(List<String> pageTypes) {
                for (String pageType : pageTypes) {
                    pageBox.addItem(pageType);
                }
            }
        });
        languageBox.addItem(world.messages().selectLanguage());
        world.service().fetchLanguages(new Reply<List<LanguageX>>() {
            public void onSuccess(List<LanguageX> languageList) {
                languages = languageList;
                for (LanguageX language : languageList) {
                    languageBox.addItem(language.getName(), language.getCode());
                }
            }
        });
        pageBox.addChangeHandler(new ChangeHandler(){
            public void onChange(ChangeEvent sender) {
                fetchPage();
            }
        });
        languageBox.addChangeHandler(new ChangeHandler(){
            public void onChange(ChangeEvent sender) {
                fetchPage();
            }
        });
        Grid choices = new Grid(1, 2);
        pageBox.setWidth("100%");
        choices.setWidget(0, 0, pageBox);
        languageBox.setWidth("100%");
        choices.setWidget(0, 1, languageBox);
        choices.setWidth("100%");
        textArea.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
            }
        });

        textArea.addKeyDownHandler(new KeyDownHandler(){
            public void onKeyDown(KeyDownEvent event) {
                
            }
        });
        
        textArea.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                      changed = true;
                }
            }
       });

        timer.scheduleRepeating(1000);
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)  {
                if (page != null) {
                    world.service().saveStaticPage(page.getId(), textArea.getText(), new Reply<StaticPageX>() {
                        public void onSuccess(StaticPageX page) {
                            setPage(page);
                        }
                    });
                }
            }
        });
        revert.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setPage(page);
            }
        });
        HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(10);
        buttons.add(revert);
        buttons.add(submit);
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(5);
        vp.add(choices);
        vp.add(new Label(world.messages().previewPage()));
        ScrollPanel previewScroll = new ScrollPanel(preview);
        previewScroll.setSize("700px", "300px");
        DecoratorPanel previewWrapper = new DecoratorPanel();
        previewWrapper.setWidget(previewScroll);
        vp.add(previewWrapper);
        vp.add(new Label(world.messages().editHtml()));
        textArea.setSize("700px", "300px");
        vp.add(textArea);
        HorizontalPanel rightSide = new HorizontalPanel();
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setWidth("100%");
        rightSide.add(buttons);
        vp.add(rightSide);
        return vp;
    }

    private void fetchPage() {
        int pageIndex = pageBox.getSelectedIndex();
        int languageIndex = languageBox.getSelectedIndex();
        if (pageIndex > 0 && languageIndex > 0) {
            world.service().fetchStaticPage(pageBox.getItemText(pageIndex), languages.get(languageIndex-1), new Reply<StaticPageX>() {
                public void onSuccess(StaticPageX staticPage) {
                    setPage(staticPage);
                }
            });
        }
    }

    private void setPage(StaticPageX page) {
        if ((this.page = page) == null) {
            textArea.setText("");
            preview.setText("");
        }
        else {
            textArea.setText(this.page.getContent());
            changed = true;
        }
    }

    private Timer timer = new Timer() {
        public void run() {
            if (changed) {
                preview.setHTML(textArea.getText());
                if (page != null) {
                    boolean same = textArea.getText().equals(page.getContent());
                    submit.setEnabled(!same);
                    revert.setEnabled(!same);
                }
                else {
                    submit.setEnabled(false);
                    revert.setEnabled(false);
                }
                changed = false;
            }
        }
    };

}
