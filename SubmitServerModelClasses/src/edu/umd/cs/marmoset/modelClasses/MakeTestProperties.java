package edu.umd.cs.marmoset.modelClasses;

import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.DEFAULT_MAKE_COMMAND;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.MAKE_COMMAND;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.MAKE_FILENAME;
import static edu.umd.cs.marmoset.modelClasses.TestPropertyKeys.STUDENT_MAKE_FILENAME;

import java.util.Properties;

import edu.umd.cs.marmoset.utilities.FileNames;

public class MakeTestProperties extends ScriptTestProperties {

    // Makefile-based only
    private String makeCommand;
    private String makefileName;
    private String studentMakefileName;

    public MakeTestProperties(Properties testProperties) {
        super(Framework.MAKE, testProperties);
        setMakeCommand(getOptionalStringProperty(MAKE_COMMAND,
                DEFAULT_MAKE_COMMAND));
        setMakefileName(getOptionalStringProperty(MAKE_FILENAME));
        setStudentMakefileName(getOptionalStringProperty(STUDENT_MAKE_FILENAME));
    }

    public String getMakeCommand() {
        return makeCommand;
    }


    protected void setMakeCommand(String makeCommand) {
        this.makeCommand = makeCommand;
        setProperty(MAKE_COMMAND, this.makeCommand);
    }

    public String getMakefileName() {
        return makefileName;
    }

    protected void setMakefileName(String makefileName) {
        this.makefileName = makefileName;
        setProperty(MAKE_FILENAME, this.makefileName);
    }

    public String getStudentMakefileName() {
        return studentMakefileName;
    }


    protected void setStudentMakefileName(String studentMakefileName) {
        this.studentMakefileName = studentMakefileName;
        setProperty(STUDENT_MAKE_FILENAME, this.studentMakefileName);
    }

    public String getStudentMakeFile() {
        return getOptionalStringProperty(STUDENT_MAKE_FILENAME);
    }
    
    @Override
	public boolean isSourceFile(String name) {
        String simpleName = FileNames.trimSourceFileName(name);
        if (name.equals(studentMakefileName) || name.equals(makefileName))
            return true;
        return super.isSourceFile(name);
    }

}
