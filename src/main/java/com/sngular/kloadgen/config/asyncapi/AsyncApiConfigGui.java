/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.asyncapi;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class AsyncApiConfigGui extends AbstractConfigGui {

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        var testElement = new AsyncApiTestElement();
        modifyTestElement(testElement);
        return testElement;
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        JPanel mainPanel = new JPanel(new GridBagLayout());
    }
}
