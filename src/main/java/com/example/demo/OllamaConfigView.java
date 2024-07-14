package com.example.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.Model;
import io.github.amithkoujalgi.ollama4j.core.models.ModelDetail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Route("/config")
public class OllamaConfigView extends VerticalLayout {

    public static final String URL = "http://localhost:11434/";
    public static final String DEMO_MODEL_KEY = "spring-ai-demo.model";

    private Select<String> allModelSelect;
    private Select<String> localModelSelect;
    private Details modelInfo = new Details();

    private static String[] OLLAMA_MODELS = new String[] {
            "alfred","all-minilm","aya","bakllava","codebooga","codegeex4","codegemma","codellama","codeqwen","codestral","codeup","command-r","command-r-plus","dbrx","deepseek-coder","deepseek-coder-v2","deepseek-llm","deepseek-v2","dolphin-llama3","dolphin-mistral","dolphin-mixtral","dolphin-phi","dolphincoder","duckdb-nsql","everythinglm","falcon","falcon2","gemma","gemma2","glm4","goliath","granite-code","internlm2","llama-pro","llama2","llama2-chinese","llama2-uncensored","llama3","llama3-chatqa","llama3-gradient","llava","llava-llama3","llava-phi3","magicoder","meditron","medllama2","megadolphin","mistral","mistral-openorca","mistrallite","mixtral","moondream","mxbai-embed-large","neural-chat","nexusraven","nomic-embed-text","notus","notux","nous-hermes","nous-hermes2","nous-hermes2-mixtral","open-orca-platypus2","openchat","openhermes","orca-mini","orca2","phi","phi3","phind-codellama","qwen","qwen2","samantha-mistral","snowflake-arctic-embed","solar","sqlcoder","stable-beluga","stable-code","stablelm-zephyr","stablelm2","starcoder","starcoder2","starling-lm","tinydolphin","tinyllama","vicuna","wizard-math","wizard-vicuna","wizard-vicuna-uncensored","wizardcoder","wizardlm","wizardlm-uncensored","wizardlm2","xwinlm","yarn-llama2","yarn-mistral","yi","zephyr"
    };

    OllamaAPI ollamaAPI = new OllamaAPI(URL);

    public OllamaConfigView() {
        localModelSelect = new Select<>("Model",s -> {
            modelInfo.setSummaryText("Model Info: "+s.getValue());
            ModelDetail md = getModelDetails(s.getValue());
            if (md != null ) {
                WebStorage.setItem(WebStorage.Storage.SESSION_STORAGE, DEMO_MODEL_KEY,s.getValue());
                modelInfo.setSummaryText(s.getValue());
                modelInfo.setContent(createForm(md));
            } else {
                modelInfo.setContent(new Span("No information available"));
            }
        });
        localModelSelect.setItems(listModelNames());

        allModelSelect = new Select<>("Available Models",s -> {
        });
        allModelSelect.setItems(List.of(OLLAMA_MODELS));

        Button pullModelButton = new Button("Pull", event -> {
            final String modelName = allModelSelect.getValue();
            final UI ui = UI.getCurrent();
            final Loader loader = new Loader();
            final HorizontalLayout layout = ((HorizontalLayout) event.getSource().getParent().get());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ui.access(() -> {
                    Notification.show("Pulling " + modelName+"...");
                    layout.add(loader);
                });
                pullModel(allModelSelect.getValue());
                ui.access(() -> {
                    Notification.show("Ready loading " + modelName+".");
                    layout.remove(loader);
                    String currentModel = localModelSelect.getValue();
                    localModelSelect.setItems(listModelNames());
                    if (currentModel != null) {
                        localModelSelect.setValue(currentModel);
                    }
                });
            });

        });


        Button listModelsButton = new Button("Refresh", event -> {
            String currentModel = localModelSelect.getValue();
            List<String> models = listModelNames();
            localModelSelect.setItems(models);
            if (currentModel != null) {
                localModelSelect.setValue(currentModel);
            }
            Notification.show("Model list updated");
        });

        WebStorage.getItem(WebStorage.Storage.SESSION_STORAGE,DEMO_MODEL_KEY, v -> {
            localModelSelect.setValue(v);
        });

        add(new HorizontalLayout(Alignment.END, localModelSelect, listModelsButton),
                modelInfo,
                new HorizontalLayout(Alignment.END, allModelSelect, pullModelButton));
    }

    private Component createForm(ModelDetail modelDetail) {
        FormLayout form = new FormLayout();

        TextField licenseField = new TextField("License", modelDetail.getLicense(), "");
        licenseField.setReadOnly(true);

        TextField modelFileField = new TextField("Model File", modelDetail.getModelFile(), "");
        modelFileField.setReadOnly(true);

        TextField parametersField = new TextField("Parameters", ""+modelDetail.getParameters(), "");
        parametersField.setReadOnly(true);

        TextField templateField = new TextField("Template", ""+modelDetail.getTemplate(), "");
        templateField.setReadOnly(true);

        TextField systemField = new TextField("System", ""+modelDetail.getSystem(), "");
        systemField.setReadOnly(true);

        TextField formatField = new TextField("Format", ""+modelDetail.getDetails().getFormat(), "");
        formatField.setReadOnly(true);

        TextField familyField = new TextField("Family", ""+modelDetail.getDetails().getFamily(), "");
        familyField.setReadOnly(true);

        TextField familiesField = new TextField("Families", ""+String.join(", ", modelDetail.getDetails().getFamilies()), "");
        familiesField.setReadOnly(true);

        TextField parameterSizeField = new TextField("Parameter Size", ""+modelDetail.getDetails().getParameterSize(), "");
        parameterSizeField.setReadOnly(true);

        TextField quantizationLevelField = new TextField("Quantization Level", ""+modelDetail.getDetails().getQuantizationLevel(), "");
        quantizationLevelField.setReadOnly(true);

        form.add(
                licenseField, modelFileField, parametersField,
                templateField, systemField, formatField,
                familyField, familiesField, parameterSizeField,
                quantizationLevelField
        );
        return form;
    }

    private void pullModel(String modelName) {
        try {
            ollamaAPI.pullModel(modelName);
        } catch (IOException | OllamaBaseException | InterruptedException | URISyntaxException e) {
            handleException(e, "");
        }
    }

    private List<String> listModelNames() {
        try {
            return ollamaAPI.listModels().stream().map(Model::getModel).toList();
        } catch (IOException | OllamaBaseException | InterruptedException | URISyntaxException e) {
            handleException(e, "");
        }
        return List.of("<No models>");
    }

    private ModelDetail getModelDetails(String modelName) {
        try {
            return modelName != null? ollamaAPI.getModelDetails(modelName): null;
        } catch (IOException | OllamaBaseException | InterruptedException | URISyntaxException e) {
            handleException(e, "Failed to get details for model '"+modelName+"'.");
        }
        return null;
    }

    private void handleException(Exception e, String message) {
        Notification.show(message);
        throw new RuntimeException(e);
    }

    /** Simple loader animation from https://cssloaders.github.io/ */
    public static class Loader extends Span {

        public Loader() {
            super();
            setClassName("loader");
            Element styles = new Element("style");
            styles.setText("""
                      width: var(--lumo-size-m);
                      height: calc(var(--lumo-size-m)/4);
                      background: var(--lumo-primary-text-color);
                      margin-top: 0;
                      display: inline-block;
                      position: relative;
                    }
                    .loader::after {
                      content: '';
                      left: 50%;
                      bottom: 0;
                      transform: translate(-50%, 0);
                      position: absolute;
                      border: calc(var(--lumo-size-m)/2.5) solid transparent;
                      border-top-color: var(--lumo-primary-text-color);
                      box-sizing: border-box;
                      animation: bump 0.4s ease-in-out infinite alternate;
                    }
                    .loader::before {
                      content: '';
                      left: 50%;
                      bottom: 25px;
                      transform: translate(-50%, 0);
                      position: absolute;
                      width: calc(var(--lumo-size-m)/3);
                      height: calc(var(--lumo-size-m)/2);
                      background: var(--lumo-primary-text-color);
                      box-sizing: border-box;
                      animation: bump 0.4s ease-in-out infinite alternate;
                    }
                    @keyframes bump {
                      0% {
                        transform: translate(-50%, 5px);
                      }
                      100% {
                        transform: translate(-50%, -5px);
                      }
                    }
                    """);
            getElement().appendChild(styles);
        }

    }

}