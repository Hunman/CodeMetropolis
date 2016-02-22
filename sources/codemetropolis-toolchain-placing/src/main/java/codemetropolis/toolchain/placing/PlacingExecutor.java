package codemetropolis.toolchain.placing;

import java.awt.EventQueue;
import java.io.IOException;

import codemetropolis.toolchain.commons.cmxml.BuildableTree;
import codemetropolis.toolchain.commons.cmxml.Validator;
import codemetropolis.toolchain.commons.cmxml.exceptions.CmxmlReaderException;
import codemetropolis.toolchain.commons.cmxml.exceptions.CmxmlWriterException;
import codemetropolis.toolchain.commons.executor.AbstractExecutor;
import codemetropolis.toolchain.commons.executor.ExecutorArgs;
import codemetropolis.toolchain.commons.util.Resources;
import codemetropolis.toolchain.placing.exceptions.LayoutException;
import codemetropolis.toolchain.placing.exceptions.NonExistentLayoutException;
import codemetropolis.toolchain.placing.layout.Layout;

public class PlacingExecutor extends AbstractExecutor {

	@Override
	public void execute(ExecutorArgs args) {
		PlacingExecutorArgs placingArgs = (PlacingExecutorArgs)args;
			
		try {
			boolean isValid = Validator.validate(placingArgs.getInputFile());
			if(!isValid) {
				printError(Resources.get("invalid_input_xml_error"));
				return;
			}
		} catch (IOException e) {
			printError(Resources.get("missing_input_xml_error"));
			return;
		}
		
		print(Resources.get("placing_reading_input"));
		BuildableTree buildables = new BuildableTree();
		try {
			buildables.loadFromFile(placingArgs.getInputFile());
		} catch (CmxmlReaderException e) {
			printError(Resources.get("cmxml_reader_error"));
			return;
		}
		print(Resources.get("placing_reading_input_done"));
		
		print(Resources.get("calculating_size_and_pos"));
		try {
			Layout layout = Layout.parse(placingArgs.getLayout());
			layout.apply(buildables);
		} catch (NonExistentLayoutException e) {
			printError(Resources.get("missing_layout_error"));
			return;
		} catch (LayoutException e) {
			printError(Resources.get("layout_error"));
			return;
		}
		print(Resources.get("calculating_size_and_pos_done"));

		print(Resources.get("placing_printing_output"));
		try {
			buildables.writeToFile(placingArgs.getOutputFile(), "placing", "rendering", "1.0");
		} catch (CmxmlWriterException e) {
			printError(Resources.get("cmxml_writer_error"));
			return;
		}
		print(Resources.get("placing_printing_output_done"));
		
		if(placingArgs.showMap()) {
			final BuildableTree b = buildables;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					CityMapGUI map = new CityMapGUI(b);
					map.setVisible(true);
				}
			});
		}
	}
	
}
