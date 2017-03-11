package gui;

import list.MyHalstead;
import list.TheFile;
import myproject.Parser;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class Controller implements Initializable {
    private Stage window; //The main window
    public BorderPane borderPane;
    public MenuBar menuBar;
    public MenuItem openFileMenu;
    public MenuItem resetFileMenu;
    public MenuItem analyzeMenu;
    public MenuItem exitMenu;
    //public MenuItem aboutMeMenu;
    public TableView<TheFile> myTable;
    public TableColumn<TheFile, String> fileNameColumn;
    public TableColumn<TheFile, String> fileStatusColumn;
    public TableColumn<TheFile, String> fileAnalysisColumn;
    public Label totalFileLabel;
    public Label totalFileNumLabel;

    private List<Parser> parserList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myTable.setId("myTable");

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileStatusColumn.setCellValueFactory(new PropertyValueFactory<>("fileStatus"));
        fileAnalysisColumn.setCellValueFactory(new PropertyValueFactory<>("analysis"));


        //Render table cell to reformat color and text.
        fileStatusColumn.setCellFactory(column -> new TableCell<TheFile, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item.equalsIgnoreCase("Successful"))
                        setStyle("-fx-text-fill: green;");
                    else
                        setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                }
                setText(item);
            }
        });

        fileAnalysisColumn.setCellFactory(column -> new TableCell<TheFile, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                //System.out.println(item + "  " + super.getTableRow().getIndex());
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item.equalsIgnoreCase("Done")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.equalsIgnoreCase("Unable") ||
                            item.equalsIgnoreCase("Error")) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                    setText(item);
                }
            }
        });

        myTable.setRowFactory(tableView -> {
            final TableRow<TheFile> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Remove File");
            removeMenuItem.getStyleClass().add("deleteMenu");
            removeMenuItem.setOnAction(event -> {
                if (showConfirmDialog("Removing the file", "Remove " + row.getItem().getFileName()))
                    myTable.getItems().remove(row.getItem());
                checkReportAvailable();
            });
            contextMenu.getItems().add(removeMenuItem);
            //Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }

    public void openFileAction() {
        try {
            FileChooser fileChooser = new FileChooser();
            configureFileChooser(fileChooser);
            List<File> fileList = fileChooser.showOpenMultipleDialog(window);
            if (fileList != null) {
                for (File aFile : fileList) {
                    double bytes = aFile.length() / 1024.00; //KB
                    //Get rid of .lnk and .url files which are not filtered.
                    if (aFile.getName().endsWith(".lnk") || aFile.getName().endsWith(".url")) {
                        loadToTable(aFile, "File extension is not allowed", "Unable");
                    } else if (bytes > 5) { //5KB
                        loadToTable(aFile, "File is too large", "Unable");
                    } else {
                        loadToTable(aFile, "Successful", "Waiting");
                    }
                }
                //Loaded with no errors
                checkReportAvailable();
            }
        } catch (Exception ex) {
            System.err.println("Error during open files.");
            showErrorDialog("Fatal Error", "Something's gone wrong, please try again.");
        }
    }

    public void resetFileAction() {
        if (showConfirmDialog("Reset All Files", "Are you sure you want to remove all files?")) {
            myTable.getItems().clear();
        }
        checkReportAvailable();
    }

    private void doRefreshAnalysisColumn() {
        myTable.getColumns().get(2).setVisible(false);
        myTable.getColumns().get(2).setVisible(true);
    }

    public void doAnalyzeAction() {
        //Parse file path to Parser.
        parserList.clear(); //Clear old parser.
        Parser tempParser;
        for (TheFile aFile : myTable.getItems()) {
            System.out.println("******************************************" +
                    "********************************************");
            tempParser = new Parser(aFile.getFilePath());
            aFile.setAnalysis(tempParser.doParse());
            parserList.add(tempParser);
            doRefreshAnalysisColumn();
            System.out.println("******************************************" +
                    "********************************************");
        }
        //Check wheter analysis is done without error.
        boolean tempErrorCheck = false;
        for (TheFile aFile : myTable.getItems()) {
            if (aFile.getAnalysis().equalsIgnoreCase("Error")) {
                tempErrorCheck = true;
                break;
            }
        }
        if (!tempErrorCheck) {
            doExportExcel();
        } else {
            showErrorDialog("Error", "Please try again.");
        }

    }

    @SuppressWarnings("all")
    private double round(double value, int places) {
        try {
            if (places < 0) throw new IllegalArgumentException();
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }catch (Exception e) {
            System.err.println("Error during round() method");
            showErrorDialog("Error", "Something's gone wrong, please try again.");
            return -1.0;
        }
    }

    private void doExportExcel() {
        Platform.runLater(() -> {
            //Start with Creating a workbook and worksheet object.
            String workSheetName = "Report";
            int columnNumber = 10 + 11; //11 is for Halstead's.

            Workbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = (XSSFSheet) workbook.createSheet(workSheetName);
            sheet.setZoom(140);

            XSSFCellStyle centerStyle = (XSSFCellStyle) workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            //Create an object of type XSSFTable.
            XSSFTable table = sheet.createTable();

            //Get CTTable object.
            CTTable cttable = table.getCTTable();

            //Let us define the required Style for the table.
            CTTableStyleInfo table_style = cttable.addNewTableStyleInfo();
            table_style.setName("TableStyleLight13");

            //Set Table Style Options.
            table_style.setShowColumnStripes(true); //showColumnStripes=0
            table_style.setShowRowStripes(true); //showRowStripes=1

            //Define the data range including headers.
            AreaReference my_data_range = new AreaReference(new CellReference(1, 1),
                    new CellReference(1 + parserList.size(), columnNumber));

            //Set Range to the Table.
            cttable.setRef(my_data_range.formatAsString());
            cttable.setDisplayName("reportTable"); //This is the display name of the table.
            cttable.setName("table1"); //This maps to "displayName" attribute in <table>, OOXML
            cttable.setId(1L); //id attribute against table as long value.

            CTTableColumns columns = cttable.addNewTableColumns();
            columns.setCount(columnNumber); //define number of columns

            //Define Header Information for the Table.
            for (int i = 0; i < columnNumber; i++) {
                CTTableColumn column = columns.addNewTableColumn();
                column.setName("Column" + i);
                column.setId(i + 1);
            }

            //--------------------------------------------------
            XSSFRow header = sheet.createRow(1);

            XSSFCell fileNameCell = header.createCell(1);
            XSSFCell methodCell = header.createCell(2);
            XSSFCell parameterCell = header.createCell(3);
            XSSFCell globalVarCell = header.createCell(4);
            XSSFCell localVarCell = header.createCell(5);
            XSSFCell assignmentStmtCell = header.createCell(6);
            XSSFCell statementCell = header.createCell(7);
            XSSFCell blockCell = header.createCell(8);
            XSSFCell loopCell = header.createCell(9);
            XSSFCell locCell = header.createCell(10);
            //Halstead's
            XSSFCell n1Cell = header.createCell(11);
            XSSFCell n2Cell = header.createCell(12);
            XSSFCell N1Cell = header.createCell(13);
            XSSFCell N2Cell = header.createCell(14);
            XSSFCell vocabCell = header.createCell(15);
            XSSFCell lengthCell = header.createCell(16);
            XSSFCell volumeCell = header.createCell(17);
            XSSFCell difficultyCell = header.createCell(18);
            XSSFCell effortCell = header.createCell(19);
            XSSFCell timeReqCell = header.createCell(20);
            XSSFCell delivBugsCell = header.createCell(21);

            fileNameCell.setCellStyle(centerStyle);
            methodCell.setCellStyle(centerStyle);
            parameterCell.setCellStyle(centerStyle);
            globalVarCell.setCellStyle(centerStyle);
            localVarCell.setCellStyle(centerStyle);
            assignmentStmtCell.setCellStyle(centerStyle);
            statementCell.setCellStyle(centerStyle);
            blockCell.setCellStyle(centerStyle);
            loopCell.setCellStyle(centerStyle);
            locCell.setCellStyle(centerStyle);
            //Halstead's
            n1Cell.setCellStyle(centerStyle);
            n2Cell.setCellStyle(centerStyle);
            N1Cell.setCellStyle(centerStyle);
            N2Cell.setCellStyle(centerStyle);
            vocabCell.setCellStyle(centerStyle);
            lengthCell.setCellStyle(centerStyle);
            volumeCell.setCellStyle(centerStyle);
            difficultyCell.setCellStyle(centerStyle);
            effortCell.setCellStyle(centerStyle);
            timeReqCell.setCellStyle(centerStyle);
            delivBugsCell.setCellStyle(centerStyle);

            fileNameCell.setCellValue("File Name");
            methodCell.setCellValue("NoM"); //Number of Methods.
            parameterCell.setCellValue("NoP"); //Number of Parameters.
            globalVarCell.setCellValue("NoGV"); //Number of Global Variables.
            localVarCell.setCellValue("NoLV"); //Number of Local Variables.
            assignmentStmtCell.setCellValue("NoAS"); //Number of Assignment Statements.
            statementCell.setCellValue("NoS"); //Number of Statements.
            blockCell.setCellValue("NoB"); //Number of Blocks.
            loopCell.setCellValue("NoL"); //Number of Loops.
            locCell.setCellValue("LOC"); //Line of Code
            //Halstead's
            n1Cell.setCellValue("n1");
            n2Cell.setCellValue("n2");
            N1Cell.setCellValue("_N1");
            N2Cell.setCellValue("_N2");
            vocabCell.setCellValue("Vocab");
            lengthCell.setCellValue("Length");
            volumeCell.setCellValue("Volume");
            difficultyCell.setCellValue("Difficulty");
            effortCell.setCellValue("Effort");
            timeReqCell.setCellValue("Time Req");
            delivBugsCell.setCellValue("Deliv Bugs");
            //--------------------------------------------------

            //***************************************************
            //************** 1 PARSER = 1 .CS FILE **************
            //***************************************************

            //Create rows according to number of files.
            XSSFRow[] fileRow = new XSSFRow[parserList.size()];
            XSSFCell[][] aCell = new XSSFCell[parserList.size()][columnNumber]; //Number of columns.
            for (int i = 0; i < parserList.size(); i++) {
                fileRow[i] = sheet.createRow(i + 2); //2 is the first row (1 is header)
                MyHalstead tempHalstead = parserList.get(i).getHalstead();
                for (int j = 0; j < columnNumber; j++) { //Number of columns.
                    aCell[i][j] = fileRow[i].createCell(j + 1);
                    aCell[i][j].setCellStyle(centerStyle);
                }
                aCell[i][0].setCellValue(parserList.get(i).getFileName());
                aCell[i][1].setCellValue(parserList.get(i).getAllMethod().size());
                aCell[i][2].setCellValue(parserList.get(i).getnumberOfParameter());
                aCell[i][3].setCellValue(parserList.get(i).getAllGlobalVariable().size());
                aCell[i][4].setCellValue(parserList.get(i).getAllLocalVariable().size());
                aCell[i][5].setCellValue(parserList.get(i).getAssignmentStmt());
                aCell[i][6].setCellValue(parserList.get(i).getAllStatement().size());
                aCell[i][7].setCellValue(parserList.get(i).getallBlock().size());
                aCell[i][8].setCellValue(parserList.get(i).getLoop());
                aCell[i][9].setCellValue(parserList.get(i).getLOC());
                //Halstead's
                aCell[i][10].setCellValue(round(tempHalstead.getDistinctOpt(), 2));
                aCell[i][11].setCellValue(round(tempHalstead.getDistinctOpr(), 2));
                aCell[i][12].setCellValue(round(tempHalstead.getNumberOfOpt(), 2));
                aCell[i][13].setCellValue(round(tempHalstead.getNumberOfOpr(), 2));
                aCell[i][14].setCellValue(round(tempHalstead.getProgramVocab(), 2));
                aCell[i][15].setCellValue(round(tempHalstead.getProgramLength(), 2));
                aCell[i][16].setCellValue(round(tempHalstead.getVolume(), 2));
                aCell[i][17].setCellValue(round(tempHalstead.getDifficulty(), 2));
                aCell[i][18].setCellValue(round(tempHalstead.getEffort(), 2));
                aCell[i][19].setCellValue(round(tempHalstead.getTimeRequired(), 2));
                aCell[i][20].setCellValue(round(tempHalstead.getDeliveredBugs(), 2));
            }
            //Auto size column. Must call after the data has been added.
            for (int i = 1; i <= columnNumber; i++)
                sheet.autoSizeColumn(i);

            File saveFileDestination;
            if (showConfirmDialog("Successful", "Analysis is complete, do you want to export a report?")) {
                FileChooser fileChooser = new FileChooser();
                configureFileChooserReport(fileChooser);
                saveFileDestination = fileChooser.showSaveDialog(window);
                if (saveFileDestination != null) {
                    if (saveFileDestination.getName().endsWith(".lnk") ||
                            saveFileDestination.getName().endsWith(".url"))
                        showErrorDialog("Error", "Please try again.");
                    try (FileOutputStream out = new FileOutputStream(saveFileDestination)) {
                        workbook.write(out);
                        showMessageDialog("Successful", "The report is saved successfully.");
                    } catch (IOException e) {
                        System.err.println("Error during export an excel file.");
                        showErrorDialog("Error", "Something's gone wrong, please try again later.");
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        System.err.println("Error during export an excel file.");
                        showErrorDialog("Error", "(NullPointerException) Please try again.");
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    private void loadToTable(File aFile, String fileStatus, String analysis) {
        TheFile x = new TheFile(aFile.getName(), fileStatus, analysis, aFile.getAbsolutePath());
        //Must override equals method.
        if (!myTable.getItems().contains(x)) {
            myTable.getItems().add(x);
        } else {
            showErrorDialog("Error", "\"" + aFile.getName() + "\" is already exist.");
        }
    }

    private static void configureFileChooser(FileChooser fileChooser) {
        fileChooser.setTitle("Open .cs File(s)");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CS files (*.cs)", "*.cs")
        );
    }

    private static void configureFileChooserReport(FileChooser fileChooser) {
        fileChooser.setTitle("Save the report");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx")
        );
    }

    private void checkReportAvailable() {
        //Report menu will be available only if all files are loaded successfully.
        boolean tempLoaded = true;
        if (myTable.getItems().size() <= 0) {
            analyzeMenu.setDisable(true);
        } else {
            for (TheFile aFile : myTable.getItems()) {
                if (!aFile.getFileStatus().equalsIgnoreCase("Successful")) {
                    tempLoaded = false;
                    break;
                }
            }
            if (tempLoaded)
                analyzeMenu.setDisable(false);
            else
                analyzeMenu.setDisable(true);
        }
        doFileCount();
    }

    private void doFileCount() {
        totalFileNumLabel.setText(myTable.getItems().size() + "");
    }

    public void closeProgram() {
        if (showConfirmDialog("Closing the program", "Are you sure?"))
            window.close();
    }

    private boolean showConfirmDialog(String aTitle, String aMessage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(aTitle);
        alert.setHeaderText(null);
        alert.setContentText(aMessage);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showErrorDialog(String aTitle, String aMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(aTitle);
        alert.setHeaderText(null);
        alert.setContentText(aMessage);
        alert.showAndWait();
    }

    @SuppressWarnings("all")
    private void showMessageDialog(String aTitle, String aMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(aTitle);
        alert.setHeaderText(null);
        alert.setContentText(aMessage);
        alert.showAndWait();
    }

    void setStage(Stage window) {
        this.window = window;
    }

}
