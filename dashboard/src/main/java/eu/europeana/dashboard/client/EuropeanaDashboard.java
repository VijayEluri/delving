package eu.europeana.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.collections.CollectionsWidget;
import eu.europeana.dashboard.client.dto.RoleX;
import eu.europeana.dashboard.client.dto.UserX;
import eu.europeana.dashboard.client.sandbox.SandboxWidget;
import eu.europeana.dashboard.client.widgets.CacheWidget;
import eu.europeana.dashboard.client.widgets.CarouselItemsWidget;
import eu.europeana.dashboard.client.widgets.ContributorWidget;
import eu.europeana.dashboard.client.widgets.GodWidget;
import eu.europeana.dashboard.client.widgets.LogWidget;
import eu.europeana.dashboard.client.widgets.PartnerWidget;
import eu.europeana.dashboard.client.widgets.RoleBasedTabPanel;
import eu.europeana.dashboard.client.widgets.SearchTermWidget;
import eu.europeana.dashboard.client.widgets.StaticPageWidget;
import eu.europeana.dashboard.client.widgets.TranslationWidget;
import eu.europeana.dashboard.client.widgets.UsersWidget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */

public class EuropeanaDashboard implements EntryPoint, DashboardWidget.World {
    private DashboardMessages messages;
    private DashboardServiceAsync service;
    private Handler handler = new Handler();
    private RootPanel rootPanel;
    private TextBox email = new TextBox();
    private PasswordTextBox password = new PasswordTextBox();
    private Label message = new Label("---*---");
    private Label errorLabel = new Label("");
    private UserX user;
    private static final String DASHBOARD_WIDTH = "100%";

    /**
     * This is the entry point method.
     */                                               
    public void onModuleLoad() {
        Reply.setHandler(handler);
        service = GWT.create(DashboardService.class);
        ServiceDefTarget serviceDefTarget = (ServiceDefTarget) service;
        serviceDefTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "DashboardService.rpc?cookie=" + generateCookie());
        messages = GWT.create(DashboardMessages.class);
        rootPanel = RootPanel.get("dashboardMain");
        rootPanel.add(createLoginPanel());

        // todo: just for speeding up testing
        email.setText("geralddejong@gmail.com");
        password.setText("mitch377");
        authenticate();
    }

    private String generateCookie() {
        StringBuilder cookie = new StringBuilder();
        for (int walk = 0; walk < 32; walk++) {
            cookie.append((char) ('A' + Math.random() * 26));
        }
        return cookie.toString();
    }

    private Widget createTabPanel() {
        RoleBasedTabPanel tabPanel = new RoleBasedTabPanel(user);
        if (user.getRole() != RoleX.ROLE_CONTENT_TESTER) { // because both sandbox and collections have a queuepoller
            tabPanel.addTab(
                    new CollectionsWidget(this),
                    messages.collectionsTab(),
                    RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new TranslationWidget(this),
                    messages.translationsTab(),
                    RoleX.ROLE_EDITOR, RoleX.ROLE_TRANSLATOR, RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new StaticPageWidget(this),
                    messages.staticPagesTag(),
                    RoleX.ROLE_TRANSLATOR, RoleX.ROLE_EDITOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new ContributorWidget(this),
                    messages.contributorsTab(),
                    RoleX.ROLE_EDITOR, RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new PartnerWidget(this),
                    messages.partnersTab(),
                    RoleX.ROLE_EDITOR, RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            // todo: fix search term add and remove to reflect new portal/pacta functionality
            tabPanel.addTab(
                    new SearchTermWidget(this),
                    messages.searchTermsTab(),
                    RoleX.ROLE_EDITOR, RoleX.ROLE_PACTA, RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new CarouselItemsWidget(this),
                    messages.carouselTab(),
                    RoleX.ROLE_EDITOR, RoleX.ROLE_CARROUSEL, RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new CacheWidget(this),
                    messages.cacheTab(),
                    RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new UsersWidget(this),
                    messages.usersTab(),
                    RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new GodWidget(this),
                    messages.GodTab(),
                    RoleX.ROLE_GOD
            );
            tabPanel.addTab(
                    new LogWidget(this),
                    messages.logTab(),
                    RoleX.ROLE_ADMINISTRATOR, RoleX.ROLE_GOD
            );
        }
        else {
            tabPanel.addTab(
                    new SandboxWidget(this),
                    messages.sandboxTab(),
                    RoleX.ROLE_CONTENT_TESTER
            );
        }
        tabPanel.selectTab(0);
        return tabPanel;
    }

    private Widget createDashboardPanel(Widget mainWidget) {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(5);
        panel.setWidth(DASHBOARD_WIDTH);
        panel.add(createTitlePanel());
        panel.add(new HTML("<hr>"));
        panel.add(errorLabel);
        panel.add(mainWidget);
        return panel;
    }

    private Widget createTitlePanel() {
        HorizontalPanel horiz = new HorizontalPanel();
        horiz.setSpacing(10);
        horiz.add(new Image("europeana-logo-small.jpg"));
        horiz.add(createTitleRight());
        return horiz;
    }

    private Widget createTitleRight() {
        Label title = new Label(messages.europeanaDashboardTitle());
        title.setStyleName("formTitle");
        String roleString = user.getRole().toString().substring("ROLE_".length());
        VerticalPanel vert = new VerticalPanel();
        vert.setSpacing(6);
        vert.add(title);
        vert.add(new Label(messages.loggedInAs(user.getUserName(), user.getEmail())));
        vert.add(new Label(messages.role(roleString)));
        return vert;
    }

    private Widget createLoginPanel() {
        message.setStyleName("loginMessage");
        FlexTable ft = new FlexTable();
        FlexTable.FlexCellFormatter format = ft.getFlexCellFormatter();
        HTML title = new HTML(messages.europeanaDashboardTitle());
        title.setStyleName("formTitle");
        ft.setWidget(0, 0, title);
        format.setColSpan(0, 0, 2);
        ft.setHTML(1, 0, messages.emailAddress());
        ft.setWidget(1, 1, email);
        ft.setHTML(2, 0, messages.password());
        ft.setWidget(2, 1, password);
        ft.setWidget(3, 0, message);
        format.setColSpan(3, 0, 2);
        Button login = new Button(messages.login());
        login.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                authenticate();
            }
        });
        password.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                if (keyCode == KeyboardListener.KEY_ENTER) {
                    authenticate();
                }
            }
        });
        ft.setWidget(4, 0, login);
        format.setColSpan(4, 0, 2);
        for (int row = 0; row <= 4; row++) {
            if (row == 3 || row == 0) {
                format.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
            }
            else {
                format.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            }
        }

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(10);
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vp.add(new Image("europeana-logo-small.jpg"));
        vp.add(ft);

        DecoratorPanel dp = new DecoratorPanel();
        dp.setWidget(vp);

        VerticalPanel all = new VerticalPanel();
        all.setWidth(DASHBOARD_WIDTH);
        all.setSpacing(10);
        all.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        all.add(dp);
        return all;
    }

    private void authenticate() {
        message.setText(messages.authenticating());
        service.login(email.getText(), password.getText(), new Reply<UserX>() {
            public void onSuccess(UserX userLoggedIn) {
                user = userLoggedIn;
                if (user != null) {
                    if (user.getRole() == RoleX.ROLE_USER) {
                        message.setText(messages.accessDenied());
                    }
                    else {
                        message.setText(messages.loginSuccessful());
                        Widget tabPanel = createTabPanel();
                        Widget dashboardPanel = createDashboardPanel(tabPanel);
                        rootPanel.clear();
                        rootPanel.add(dashboardPanel);
                    }
                }
                else {
                    message.setText(messages.emailAddressPasswordNotFound());
                    password.setText("");
                }
            }
        });
    }

    public UserX user() {
        return user;
    }

    public DashboardMessages messages() {
        return messages;
    }

    public DashboardServiceAsync service() {
        return service;
    }

    private class Handler implements Reply.FailureHandler {
        public void onFailure(Throwable caught) {
            errorLabel.setText(messages.serverProblem());
            Timer timer = new Timer() {
                public void run() {
                    errorLabel.setText("");
                }
            };
            timer.schedule(5000);
        }
    }
}