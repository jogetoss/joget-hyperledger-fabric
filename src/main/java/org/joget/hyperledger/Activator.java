package org.joget.hyperledger;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    @Override
    public void start(BundleContext context) {
        registrationList = new ArrayList<>();

        // Register plugins
        registrationList.add(context.registerService(FabricTool.class.getName(), new FabricTool(), null));
        registrationList.add(context.registerService(FabricListBinder.class.getName(), new FabricListBinder(), null));
        registrationList.add(context.registerService(FabricFormBinder.class.getName(), new FabricFormBinder(), null));
    }

    @Override
    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}