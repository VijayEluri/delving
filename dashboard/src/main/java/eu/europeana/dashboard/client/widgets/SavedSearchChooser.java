package eu.europeana.dashboard.client.widgets;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.SavedSearchX;
import eu.europeana.dashboard.client.dto.UserX;

import java.util.Iterator;
import java.util.List;

/**
 * This is a component that lets you choose from anybody's saved searches
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SavedSearchChooser extends DashboardWidget {
    private Owner owner;
    private List<SavedSearchX> searches;
    private ListBox listBox;
    private Button selectButton;

    public SavedSearchChooser(World world, Owner owner) {
        super(world);
        this.owner = owner;
    }

    public interface Owner {
        boolean avoidSearch(SavedSearchX savedSearch);
        void selectSearch(SavedSearchX savedSearch);
    }

    private void setSearches(List<SavedSearchX> searches) {
        if ((this.searches = searches) == null) {
            listBox.clear();
            selectButton.setEnabled(false);
        }
        else {
            listBox.clear();
            Iterator<SavedSearchX> walk = searches.iterator();
            while (walk.hasNext()) {
                SavedSearchX search = walk.next();
                if (owner.avoidSearch(search)) {
                    walk.remove();
                }
                else {
                    if (listBox.getItemCount() == 0) {
                        listBox.addItem(world.messages().selectSavedSearch());
                    }
                    listBox.addItem(search.getQueryString());
                }
            }
            if (listBox.getItemCount() == 0) {
                listBox.addItem(world.messages().noSavedSearches());
            }
            selectButton.setEnabled(false);
        }
    }

    protected Widget createWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        listBox = new ListBox();
        listBox.setWidth("300px");
        selectButton = new Button(world.messages().select());
        selectButton.setEnabled(false);
        panel.setSpacing(6);
        UserChooser userChooser = new UserChooser(world);
        userChooser.setListener(new UserChooser.Listener() {
            public void userSelected(UserX user) {
                world.service().fetchSavedSearches(user.getId(), new Reply<List<SavedSearchX>>() {
                    public void onSuccess(List<SavedSearchX> result) {
                        setSearches(result);
                    }
                });
            }
        });
        panel.add(userChooser.getWidget());
        listBox.addChangeListener(new ChangeListener() {
            public void onChange(Widget sender) {
                int select = listBox.getSelectedIndex();
                selectButton.setEnabled(select > 0);
            }
        });
        panel.add(listBox);
        selectButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                int index = listBox.getSelectedIndex()-1;
                owner.selectSearch(searches.get(index));
                listBox.removeItem(index);
                searches.remove(index);
                selectButton.setEnabled(false);
                listBox.setSelectedIndex(0);
            }
        });
        panel.add(selectButton);
        return panel;
    }
}