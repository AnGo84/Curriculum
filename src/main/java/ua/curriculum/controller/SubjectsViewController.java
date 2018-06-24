package ua.curriculum.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.curriculum.MainApp;
import ua.curriculum.dao.SubjectDao;
import ua.curriculum.dao.TeacherDao;
import ua.curriculum.export.ExportData;
import ua.curriculum.export.ExportFileType;
import ua.curriculum.export.ExportServices;
import ua.curriculum.fx.ComboBoxItem;
import ua.curriculum.fx.DialogText;
import ua.curriculum.fx.Dialogs;
import ua.curriculum.fx.ImageResources;
import ua.curriculum.model.Subject;
import ua.curriculum.model.Teacher;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectsViewController {
    FilteredList<Subject> filteredData;

    private Logger logger = LogManager.getLogger(this.getClass());
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabTable;
    @FXML
    private Tab tabView;
    @FXML
    private Tab tabEdit;
    @FXML
    private Tab tabFilter;
    @FXML
    private HBox hBoxEdit;
    @FXML
    private HBox hBoxOkCancel;
    @FXML
    private HBox hBoxFilter;
    @FXML
    private HBox hBoxNavigation;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonNew;
    @FXML
    private Button buttonEdit;
    @FXML
    private Button buttonDelete;
    @FXML
    private Button buttonFirst;
    @FXML
    private Button buttonPrior;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonLast;
    @FXML
    private Button buttonRefresh;
    @FXML
    private Button buttonFilter;
    @FXML
    private Button buttonNewTeacher;
    @FXML
    private Button buttonDeleteTeacher;
    @FXML
    private Button buttonShowTeachers;

    @FXML
    private TableView<Subject> tableViewData;
    @FXML
    private TableView<Teacher> tableViewTeacherData;
    @FXML
    private TableColumn<Subject, Integer> tableColumnId;
    @FXML
    private TableColumn<Subject, String> tableColumnFullName;
    @FXML
    private TableColumn<Subject, String> tableColumnShortName;
    @FXML
    private TableColumn<Subject, String> tableColumnCode;
    @FXML
    private TableColumn<Teacher, Integer> tableColumnTeacherID;
    @FXML
    private TableColumn<Teacher, String> tableColumnTeacherPIP;
    @FXML
    private TableColumn<Teacher, String> tableColumnTeacherPosition;
    @FXML
    private TextField textFViewId;
    @FXML
    private TextField textFViewFullName;
    @FXML
    private TextField textFViewShortName;
    @FXML
    private TextField textFViewCode;
    @FXML
    private TextField textFEditId;
    @FXML
    private TextField textFEditFullName;
    @FXML
    private TextField textFEditShortName;
    @FXML
    private TextField textFEditCode;
    @FXML
    private TextField textFFilterId;
    @FXML
    private TextField textFFilterFullName;
    @FXML
    private TextField textFFilterShortName;
    @FXML
    private TextField textFFilterCode;
    @FXML
    private ComboBox<ComboBoxItem> comboBoxFilterTeacher = new ComboBox<>();

    @FXML
    private MenuButton menuButtonReports;
    @FXML
    private MenuItem menuItemExcel;
    @FXML
    private MenuItem menuItemJasper;

    private SubjectDao subjectDao;
    private TeacherDao teacherDao;
    private ObservableList<Subject> subjectObservableList = FXCollections.observableArrayList();
    private ObservableList<Teacher> selectedSubjectTeacherData = FXCollections.observableArrayList();

    private ObservableList<ComboBoxItem> observableListTeachers = FXCollections.observableArrayList();
    private MainApp mainApp;

    private Stage dialogStage;
    private boolean isEdited;
    private boolean isFilter;

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        initHBoxes(true);

        subjectDao = new SubjectDao(mainApp.getConnection());
        teacherDao = new TeacherDao(mainApp.getConnection());

        try {
            refreshTableData();

            List<ComboBoxItem> list = teacherDao.findAllComboBoxData();
            observableListTeachers.add(new ComboBoxItem("0", ""));
            observableListTeachers.addAll(list);
            comboBoxFilterTeacher.setItems(observableListTeachers);

        } catch (SQLException e) {
            Dialogs.showErrorDialog(e, new DialogText("Помилка отримання даних", "",
                                                      "Неможливо отримати данні з таблиці 'Предмети'"), logger);
        }
    }

    private void refreshTableData() throws SQLException {
        subjectObservableList.clear();
        tableViewData.getItems().removeAll(tableViewData.getItems());

        subjectObservableList.addAll(subjectDao.findAllData());
        tableViewData.setItems(subjectObservableList);
    }

    private void refreshTeachersData() {
        selectedSubjectTeacherData.clear();
        tableViewTeacherData.getItems().removeAll(tableViewTeacherData.getItems());
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        tabPane.setStyle("-fx-open-tab-animation: NONE; -fx-close-tab-animation: NONE;");

        initButtonsIcons();
        initButtonsToolTip();



        initTabPane();


        initColumnValueFactories();

        showSubjectDetails(null);

        initComponentListeners();
    }

    private void initComponentListeners() {
        tableViewData.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showSubjectDetails(newValue));
    }

    private void initColumnValueFactories() {
        tableColumnId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tableColumnFullName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        tableColumnShortName.setCellValueFactory(cellData -> cellData.getValue().shortNameProperty());
        tableColumnCode.setCellValueFactory(cellData -> cellData.getValue().codeProperty());

        tableColumnTeacherID.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        tableColumnTeacherPIP
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullPIP()));
        tableColumnTeacherPosition.setCellValueFactory(cellData -> cellData.getValue().positionProperty());

    }

    private void initTabPane() {
        tabPane.getTabs().removeAll(tabPane.getTabs());
        tabPane.getTabs().addAll(tabTable, tabView);
        tabPane.getSelectionModel().select(tabTable);
    }

    private void initHBoxes(boolean isView) {
        if (mainApp!=null) {
            hBoxEdit.setVisible(isView && mainApp.getLoginUser().getUserType().getId()==1);
        }
        else{
            hBoxEdit.setVisible(isView);
        }
        hBoxOkCancel.setVisible(!isView);
        hBoxFilter.setVisible(isView);
        hBoxNavigation.setVisible(isView);
    }

    private void initButtonsToolTip() {
        buttonNew.setTooltip(new Tooltip("Додати запис"));
        buttonEdit.setTooltip(new Tooltip("Редагувати запис"));
        buttonDelete.setTooltip(new Tooltip("Вилучити запис"));

        buttonNewTeacher.setTooltip(new Tooltip("Додати вчителя"));
        buttonDeleteTeacher.setTooltip(new Tooltip("Вилучити вчителя"));


        buttonFilter.setTooltip(new Tooltip("Відфільтрувати записи"));

        buttonOk.setTooltip(new Tooltip("Підтвердити зміни"));
        buttonCancel.setTooltip(new Tooltip("Відмінити зміни"));

        buttonFirst.setTooltip(new Tooltip("Перший запис"));
        buttonPrior.setTooltip(new Tooltip("Попередній запис"));
        buttonNext.setTooltip(new Tooltip("Наступний запис"));
        buttonLast.setTooltip(new Tooltip("Останній запис"));
        buttonRefresh.setTooltip(new Tooltip("Оновити дані"));

        buttonShowTeachers.setTooltip(new Tooltip("Показати довідник студентів"));

        menuButtonReports.setTooltip(new Tooltip("Звіти"));
    }

    private void initButtonsIcons() {
        buttonNew.setGraphic(new ImageView(ImageResources.getButtonPlus()));
        buttonEdit.setGraphic(new ImageView(ImageResources.getButtonEdit()));
        buttonDelete.setGraphic(new ImageView(ImageResources.getButtonDelete()));

        buttonNewTeacher.setGraphic(new ImageView(ImageResources.getButtonPlus()));
        buttonDeleteTeacher.setGraphic(new ImageView(ImageResources.getButtonDelete()));


        buttonFilter.setGraphic(new ImageView(ImageResources.getButtonFilter()));

        buttonOk.setGraphic(new ImageView(ImageResources.getButtonOk()));
        buttonCancel.setGraphic(new ImageView(ImageResources.getButtonCancel()));

        buttonFirst.setGraphic(new ImageView(ImageResources.getButtonFirst()));
        buttonPrior.setGraphic(new ImageView(ImageResources.getButtonPrior()));
        buttonNext.setGraphic(new ImageView(ImageResources.getButtonNext()));
        buttonLast.setGraphic(new ImageView(ImageResources.getButtonLast()));
        buttonRefresh.setGraphic(new ImageView(ImageResources.getButtonRefresh()));

        buttonShowTeachers.setGraphic(new ImageView(ImageResources.getButtonView()));

        menuButtonReports.setGraphic(new ImageView(ImageResources.getReportIcon()));
        menuItemExcel.setGraphic(new ImageView(ImageResources.getXlsx16Icon()));
        menuItemJasper.setGraphic(new ImageView(ImageResources.getReportsIcon()));
    }


    public void onButtonNew(ActionEvent actionEvent) {
        textFEditId.setText("");
        textFEditFullName.setText("");
        textFEditShortName.setText("");
        textFEditCode.setText("");

        tabPane.getTabs().removeAll(tabPane.getTabs());
        tabPane.getTabs().addAll(tabEdit);
        initHBoxes(false);

        isEdited = true;

    }

    public void onButtonEdit(ActionEvent actionEvent) {
        int selectedIndex = tableViewData.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Subject subject = tableViewData.getItems().get(selectedIndex);
            textFEditId.setText(String.valueOf(subject.getId()));
            textFEditFullName.setText(subject.getName());
            textFEditShortName.setText(subject.getShortName());
            textFEditCode.setText(subject.getCode());

            tabPane.getTabs().removeAll(tabPane.getTabs());
            tabPane.getTabs().addAll(tabEdit);

            initHBoxes(false);

            isEdited = true;
        } else {
            Dialogs.showMessage(Alert.AlertType.ERROR, new DialogText("Помилка редагування даних", "Запис не вибраний",
                                                                      "Будь ласка виберіть запис в таблиці"), null);
        }
    }

    public void onButtonDelete(ActionEvent actionEvent) {
        int selectedIndex = tableViewData.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Subject subject = tableViewData.getItems().get(selectedIndex);
            if (Dialogs.showConfirmDialog(new DialogText("Підтвердження дії", "Бажаєте вілучити запис",
                                                         "Ви дійсно бажаєте вилучити '" + subject.getName() + "'"),
                                          logger)) {
                try {
                    if (subjectDao.deleteById(subject.getId())) {
                        tableViewData.getItems().remove(selectedIndex);
                    }

                } catch (SQLException e) {
                    Dialogs.showErrorDialog(e, new DialogText("Помилка вилучення даних", "",
                                                              "Неможливо вилучити '" + subject.getName() + "'"),
                                            logger);
                }
            }
        } else {
            Dialogs.showMessage(Alert.AlertType.ERROR, new DialogText("Помилка вилучення даних", "Запис не вибраний",
                                                                      "Будь ласка виберіть запис в таблиці"), null);
        }
    }


    public void onButtonNewTeacher(ActionEvent actionEvent) {
        int selectedIndex = tableViewData.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Subject subject = tableViewData.getItems().get(selectedIndex);
            ComboBoxItem item = null;
            try {
                item = mainApp.showSearchFormDialog(teacherDao.findNewTeachersForSubject(subject.getId()));
                if (item != null && item.getObjectId() != null) {
                    int teacherId = Integer.parseInt(item.getObjectId());
                    if (subjectDao.insertIntoTeachersSubjects(subject.getId(), teacherId)) {
                        refreshTeachersData();

                        selectedSubjectTeacherData.addAll(teacherDao.findSubjectTeachersById(subject.getId()));
                        tableViewTeacherData.setItems(selectedSubjectTeacherData);
                    }
                }
            } catch (SQLException e) {
                Dialogs.showErrorDialog(e, new DialogText("Помилка додавання даних", "",
                                                          "Неможливо додати '" + item.getObjectDisplayName() + "'"),
                                        logger);
            }

        } else {
            Dialogs.showMessage(Alert.AlertType.ERROR, new DialogText("Помилка редагування даних", "Запис не вибраний",
                                                                      "Будь ласка виберіть запис в таблиці"), null);
        }
    }

    public void onButtonDeleteTeacher(ActionEvent actionEvent) {
        int selectedSubjectIndex = tableViewData.getSelectionModel().getSelectedIndex();
        int selectedTeacherIndex = tableViewTeacherData.getSelectionModel().getSelectedIndex();

        if (selectedSubjectIndex >= 0 && selectedTeacherIndex >= 0) {
            Subject subject = tableViewData.getItems().get(selectedSubjectIndex);
            Teacher teacher = tableViewTeacherData.getItems().get(selectedTeacherIndex);
            if (Dialogs.showConfirmDialog(new DialogText("Підтвердження дії", "Бажаєте вілучити запис",
                                                         "Ви дійсно бажаєте вилучити '" + teacher.getFullPIP() + "'"),
                                          logger)) {
                try {
                    if (subjectDao.deleteFromTeachersSubjects(subject.getId(), teacher.getId())) {
                        tableViewTeacherData.getItems().remove(selectedTeacherIndex);
                    }

                } catch (SQLException e) {
                    Dialogs.showErrorDialog(e, new DialogText("Помилка вилучення даних", "",
                                                              "Неможливо вилучити '" + teacher.getFullPIP() + "'"),
                                            logger);
                }
            }
        } else {
            Dialogs.showMessage(Alert.AlertType.ERROR, new DialogText("Помилка вилучення даних", "Запис не вибраний",
                                                                      "Будь ласка виберіть запис в таблиці"), null);
        }
    }

    public void onButtonOk(ActionEvent actionEvent) {
        //initTabPane();
        //initHBoxes(true);

        boolean isInsert = (textFEditId.getText() == null || textFEditId.getText().equals("")) ? true : false;
        if (isEdited) {
            if (isInputValid()) {
                Subject subject = new Subject();
                subject.setName(textFEditFullName.getText());
                subject.setShortName(textFEditShortName.getText());
                subject.setCode(textFEditCode.getText());

                if (isInsert) {
                    try {
                        if (subjectDao.insert(subject)) {
                            refreshTableData();
                        }
                    } catch (SQLException e) {
                        Dialogs.showErrorDialog(e, new DialogText("Помилка додавання даних", "",
                                                                  "Неможливо додати '" + subject.getName() + "'"),
                                                logger);
                    }
                } else if (!isInsert) {
                    subject.setId(Integer.parseInt(textFEditId.getText()));
                    try {
                        if (subjectDao.update(subject)) {
                            refreshTableData();
                        }
                    } catch (SQLException e) {
                        Dialogs.showErrorDialog(e, new DialogText("Помилка редагування даних", "",
                                                                  "Неможливо змінити '" + subject.getName() + "'"),
                                                logger);
                    }

                }

                isEdited = false;

                initTabPane();
                initHBoxes(true);
            }
        } else {
            // for filter
            // logger.info("hasFilterPattern()= " + hasFilterPattern());

            filteredData = new FilteredList<>(subjectObservableList, p -> true);

            if (!hasFilterPattern()) {
                filteredData.setPredicate(subject -> true);
            } else {
                filteredData.setPredicate(subject -> compareForFilter(subject));
            }

            SortedList<Subject> sortedData = new SortedList<>(filteredData);

            sortedData.comparatorProperty().bind(tableViewData.comparatorProperty());

            tableViewData.setItems(sortedData);


            isFilter = false;

            initTabPane();
            initHBoxes(true);
        }
    }

    private boolean hasFilterPattern() {
        if (textFFilterId.getText() != null && textFFilterId.getText().length() > 0) {
            return true;
        }
        if (textFFilterFullName.getText() != null && textFFilterFullName.getText().length() > 0) {
            return true;
        }
        if (textFFilterShortName.getText() != null && textFFilterShortName.getText().length() > 0) {
            return true;
        }
        if (textFFilterCode.getText() != null && textFFilterCode.getText().length() > 0) {
            return true;
        }

        if (comboBoxFilterTeacher.getValue() != null && !comboBoxFilterTeacher.getValue().getObjectId().equals("0")) {
            return true;
        }

        return false;
    }

    private boolean compareForFilter(Subject subject) {
        //logger.info(subject);
        if (textFFilterId.getText() != null && textFFilterId.getText().length() > 0) {
            if (!String.valueOf(subject.getId()).toUpperCase().contains(textFFilterId.getText().toUpperCase())) {
                return false;
            }
        }
        if (textFFilterFullName.getText() != null && textFFilterFullName.getText().length() > 0) {
            if (!subject.getName().toUpperCase().contains(textFFilterFullName.getText().toUpperCase())) {
                return false;
            }
        }
        if (textFFilterShortName.getText() != null && textFFilterShortName.getText().length() > 0) {
            if (!subject.getShortName().toUpperCase().contains(textFFilterShortName.getText().toUpperCase())) {
                return false;
            }
        }
        if (textFFilterCode.getText() != null && textFFilterCode.getText().length() > 0) {
            if (!subject.getCode().toUpperCase().contains(textFFilterCode.getText().toUpperCase())) {
                return false;
            }
        }

        if (comboBoxFilterTeacher.getValue() != null && !comboBoxFilterTeacher.getValue().getObjectId().equals("0")) {
            try {
                boolean groupHasStudent = subjectDao.getTeachersSubjectsId(subject.getId(), Integer.parseInt(
                        comboBoxFilterTeacher.getValue().getObjectId()));

                if (!groupHasStudent) {
                    return false;
                }
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
        }

        //logger.info("TRUE: "  + subject);
        return true;
    }

    public void onButtonCancel(ActionEvent actionEvent) {
        initTabPane();
        initHBoxes(true);
        if (isFilter && filteredData!=null) {
            filteredData.setPredicate(s -> true);
        }
        isEdited = false;
        isFilter = false;
    }

    public void onButtonFilter(ActionEvent actionEvent) {
        isEdited = false;
        isFilter = true;
        tabPane.getTabs().removeAll(tabPane.getTabs());
        tabPane.getTabs().addAll(tabFilter);
        initHBoxes(false);
    }

    public void onButtonFirst(ActionEvent actionEvent) {
        tableViewData.getSelectionModel().selectFirst();
        tableViewData.scrollTo(tableViewData.getSelectionModel().getSelectedIndex());
    }

    public void onButtonPrior(ActionEvent actionEvent) {
        tableViewData.getSelectionModel().selectPrevious();
        tableViewData.scrollTo(tableViewData.getSelectionModel().getSelectedIndex());
    }

    public void onButtonNext(ActionEvent actionEvent) {
        tableViewData.getSelectionModel().selectNext();
        tableViewData.scrollTo(tableViewData.getSelectionModel().getSelectedIndex());
    }

    public void onButtonLast(ActionEvent actionEvent) {
        tableViewData.getSelectionModel().selectLast();
        tableViewData.scrollTo(tableViewData.getSelectionModel().getSelectedIndex());
    }

    public void onButtonRefresh(ActionEvent actionEvent) {
        tableViewData.refresh();
    }

    private void showSubjectDetails(Subject subject) {
        refreshTeachersData();
        if (subject != null) {
            textFViewId.setText(String.valueOf(subject.getId()));
            textFViewFullName.setText(subject.getName());
            textFViewShortName.setText(subject.getShortName());
            textFViewCode.setText(subject.getCode());

            try {
                selectedSubjectTeacherData.addAll(teacherDao.findSubjectTeachersById(subject.getId()));
            } catch (SQLException e) {
                Dialogs.showErrorDialog(e, new DialogText("Помилка отримання даних", "",
                                                          "Неможливо отримати дані про Викладачів "), logger);
            }
            tableViewTeacherData.setItems(selectedSubjectTeacherData);

        } else {
            textFViewId.setText("");
            textFViewFullName.setText("");
            textFViewShortName.setText("");
            textFViewCode.setText("");
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (textFEditFullName.getText() == null || textFEditFullName.getText().length() == 0) {
            errorMessage += "Невірна назва!\n";
        }
        if (textFEditShortName.getText() == null || textFEditShortName.getText().length() == 0) {
            errorMessage += "Невірна скорочена назва!\n";
        }

        if (textFEditCode.getText() == null || textFEditCode.getText().length() == 0) {
            errorMessage += "Невірне позначення!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Dialogs.showMessage(Alert.AlertType.ERROR,
                                new DialogText("Помилка заповнення", "Будь ласка заповнить помилкові поля",
                                               errorMessage), null);
            //alert.initOwner(dialogStage);
            return false;
        }
    }

    public void onButtonShowTeachers(ActionEvent actionEvent) {
        ComboBoxItem item = mainApp.showSearchFormDialog(observableListTeachers);
        if (item != null) {
            comboBoxFilterTeacher.getSelectionModel().select(item);
        }
    }

    public void onMenuItemExcel(ActionEvent actionEvent) {
        ExportData exportData = new ExportData();
        Subject newSubject = new Subject();
        exportData.setFieldList(newSubject.getFieldList());
        exportData.setExportFileType(ExportFileType.XLSX);

        List<Object[]> objects = new ArrayList<>();

        ObservableList<Subject> subjects;
        subjects = getReportSubjects();

        for (Subject subject : subjects) {
            objects.add(subject.getObjects());
        }

        exportData.setTableData(objects);

        try {
            ExportServices.exportData(dialogStage, exportData);
            if (exportData.getFile()!=null && exportData.getFile().exists()) {
                Dialogs.showMessage(Alert.AlertType.INFORMATION, new DialogText("Експорт даних", "Файл '" + exportData.getFile()
                        .getName() + "' збережено успішно", ""), logger);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            Dialogs.showErrorDialog(e, new DialogText("Експорт даних", "Файл не збережено", ""), logger);
        }
    }

    public ObservableList<Subject> getReportSubjects() {
        ObservableList<Subject> subjects;
        if (filteredData != null ){
            subjects=filteredData;
        }
        else {
            subjects = subjectObservableList;
        }
        return subjects;
    }

    public void onMenuItemJasper(ActionEvent actionEvent) throws IOException {
        //MainApp.class.getResourceAsStream("/jasperReports/GroupsStudents.jrxml");
        PreparedStatement preparedStatement = null;
        ObservableList<Subject> subjects = getReportSubjects();

        try {
            preparedStatement = mainApp.getConnection().prepareStatement("DELETE FROM temp_report");
            preparedStatement.executeUpdate();
            for (Subject subject : subjects) {
                preparedStatement =
                        mainApp.getConnection().prepareStatement("INSERT INTO temp_report(id) " + "values(?)");
                preparedStatement.setInt(1, subject.getId());
                preparedStatement.executeUpdate();
            }
            // First, compile jrxml file.
            JasperReport jasperReport = JasperCompileManager
                    .compileReport(MainApp.class.getResourceAsStream("/jasperReports/SubjectTeacher.jrxml"));
            // Parameters for report
            Map<String, Object> parameters = new HashMap<String, Object>();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, mainApp.getConnection());
            JasperViewer jasperViewer = new JasperViewer(jasperPrint);

            jasperViewer.viewReport(jasperPrint, false);


        } catch (SQLException | JRException e) {

            Dialogs.showErrorDialog(e, new DialogText("Експорт даних", "Файл не збережено", e.getMessage()), logger);
        }
    }
}
