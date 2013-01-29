/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.web.publish;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.geoserver.catalog.AttributionInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.publish.LayerConfigurationPanel;

/**
 * Configures a {@link LayerInfo} geo-search related metadata
 */
@SuppressWarnings("serial")
public class AttributionLayerConfigPanel extends LayerConfigurationPanel{
    public AttributionLayerConfigPanel(String id, IModel model){
        super(id, model);

        LayerInfo layer = (LayerInfo) model.getObject();

        if (layer.getAttribution() == null) {
            layer.setAttribution(
                GeoServerApplication.get().getCatalog().getFactory().createAttribution()
            );
        }

        AttributionInfo attr = layer.getAttribution();

        add(new TextField("wms.attribution.title", 
            new PropertyModel(model, "attribution.title")
        ));

        final TextField href = new TextField("wms.attribution.href", 
            new PropertyModel(model, "attribution.href")
        );
        href.add(new UrlValidator());
        href.setOutputMarkupId(true);
        add(href);

        final TextField logo = new TextField("wms.attribution.logo", 
            new PropertyModel(model, "attribution.logoURL")
        );
        logo.add(new UrlValidator());
        logo.setOutputMarkupId(true);
        add(logo);

        final TextField<String> type = new TextField<String>("wms.attribution.type",
            new PropertyModel<String>(model, "attribution.logoType")
        );
        type.setOutputMarkupId(true);
        add(type);

        final TextField<Integer> height = new TextField<Integer>("wms.attribution.height", 
            new PropertyModel<Integer>(model, "attribution.logoHeight"),
            Integer.class
        );
        height.add(new MinimumValidator<Integer>(0));
        height.setOutputMarkupId(true);
        add(height);

        final TextField<Integer> width = new TextField<Integer>("wms.attribution.width",
            new PropertyModel<Integer>(model, "attribution.logoWidth"),
            Integer.class
        );
        width.add(new MinimumValidator<Integer>(0));
        width.setOutputMarkupId(true);
        add(width);

        add(new AjaxSubmitLink("verifyImage") {
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (logo.getDefaultModelObjectAsString() != null) {
                    try { 
                        URL url = new URL(logo.getDefaultModelObjectAsString());
                        URLConnection conn = url.openConnection();
                        type.setModelObject(conn.getContentType());
                        BufferedImage image = ImageIO.read(conn.getInputStream());
                        height.setModelObject(image.getHeight());
                        width.setModelObject(image.getWidth());
                    } catch (Exception e) {
                    }
                }
                
                target.add(type, height, width);
            }

			@Override
			protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
				// TODO: add form validation component to page.
			}
        });
    }
}
