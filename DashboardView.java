package view;

import controller.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class DashboardView {

    @FXML private Label whoLabel;
    @FXML private TabPane tabs;

    private AppContext context;
    private Stage stage;

    static void show(AppContext context, Stage stage) {
        ViewLoader.Loaded<DashboardView> loaded = ViewLoader.load("Dashboard.fxml");
        loaded.controller.context = context;
        loaded.controller.stage = stage;
        loaded.controller.loadTabs();

        ViewLoader.show(stage, loaded.root, "SM BANKING");
    }

    private void loadTabs() {
        boolean teller = context.session().getCurrentUser().isTeller();
        whoLabel.setText(context.session().getCurrentUser().getUsername());
        ViewLoader.Loaded<AccountsView> accounts = ViewLoader.load("Accounts.fxml");
        accounts.controller.setContext(context);
        tabs.getTabs().add(new Tab("Accounts", accounts.root));

        ViewLoader.Loaded<TransactionsView> transactions = ViewLoader.load("Transactions.fxml");
        transactions.controller.setContext(context);
        tabs.getTabs().add(new Tab("Transactions", transactions.root));

        if (teller) {
            ViewLoader.Loaded<CustomersView> customers = ViewLoader.load("Customers.fxml");
            customers.controller.setContext(context);
            tabs.getTabs().add(new Tab("Customers", customers.root));
        }
    }

    @FXML
    private void onLogout() {
        context.auth().logout();
        LoginView.show(context, stage);
    }
}
