package com.tommytony.war;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.translator.DataTranslator;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.spongepowered.api.data.DataQuery.of;

class YamlTranslator implements DataTranslator<String> {
    private Yaml yaml;

    YamlTranslator() {
        this.yaml = new Yaml();
    }

    @Override
    public String translateData(DataView container) {
        return yaml.dump(container.getMap(DataQuery.of()).get());
    }

    @Override
    public void translateContainerToData(String node, DataView container) {
        node = translateData(container);
    }

    @Override
    public DataContainer translateFrom(String node) {
        DataContainer container = new MemoryDataContainer();
        if (node != null && !node.isEmpty()) {
            Object data = yaml.load(node);
            if (data instanceof Map) {
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) data).entrySet()) {
                    container.set(of('.', entry.getKey().toString()), entry.getValue());
                }
            }
        }
        return container;
    }
}
