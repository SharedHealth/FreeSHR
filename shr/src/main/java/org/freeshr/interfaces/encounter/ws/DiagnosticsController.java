package org.freeshr.interfaces.encounter.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.health.ApplicationHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/diagnostics")
public class DiagnosticsController {
    @Autowired
    private Environment environment;
    @Autowired
    private TraceRepository traceRepository;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public Health health() {
        return new ApplicationHealthIndicator().health();
    }

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public Collection<Metric<?>> metrics() {
        Collection<Metric<?>> metrics = new ArrayList<>();
        metrics.addAll(new TomcatPublicMetrics().metrics());
        metrics.addAll(new SystemPublicMetrics().metrics());
        return metrics;
    }

    @RequestMapping(value = "/env", method = RequestMethod.GET)
    public Map<String, Object> env() {
        EnvironmentEndpoint environmentEndpoint = new EnvironmentEndpoint();
        environmentEndpoint.setEnvironment(environment);
        return environmentEndpoint.invoke();
    }

    @RequestMapping(value = "/dump", method = RequestMethod.GET)
    public List<ThreadInfo> dump() {
        return new DumpEndpoint().invoke();
    }

    @RequestMapping(value = "/trace", method = RequestMethod.GET)
    public List<Trace> trace() {
        return new TraceEndpoint(traceRepository).invoke();
    }


}
