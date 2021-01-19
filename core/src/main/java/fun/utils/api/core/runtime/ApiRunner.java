package fun.utils.api.core.runtime;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ApiRunner {

    @Setter @Getter
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
