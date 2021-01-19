package fun.utils.api.core.runtime;

import org.checkerframework.checker.initialization.qual.Initialized;

public class ApiRunner {

    RunContext runContext;

    public void doInitialize() throws Exception{

    }

    public void onEnter() throws Exception{

    }

    public void doValidate() throws Exception {

    }

    public void onExecute() throws Exception {

    }

    public void execute() throws Exception{

    }

    public void onReturn() throws Exception {

    }

    public void run() throws Exception {

        doInitialize();

        onEnter();

        doValidate();

        onExecute();

        execute();

        onReturn();

    }

}
