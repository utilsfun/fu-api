package fun.utils.api.core.script;


import javafx.util.Callback;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroovySrcDefaultLoader implements Callback<String, GroovySource> {

    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */

    @SneakyThrows
    @Override
    public GroovySource call(String param) {

        GroovySource result = new GroovySource();
        result.setId(param);

        URL url = this.getClass().getClassLoader().getResource(param);

        if (url == null) {
            url = new URL(param);
        }

        if (url != null) {

            String groovy = IOUtils.toString(url, Charset.defaultCharset()).trim();
            GroovySource groovySource = GroovyUtils.sourceOf(null,groovy);
            result.getImports().addAll(groovySource.getImports());
            result.setSource(groovySource.getSource());
        }

        return result;

    }
}
