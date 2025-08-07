/*******************************************************************************
 * Copyright (c) 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter.utils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A utility class for retrieving and formatting localized messages from a resource bundle.
 * <p>
 * Used to support internationalization (i18n) by binding parameters to message templates.
 * </p>
 */
public class Messages {

    /**
     * Default constructor.
     */
    public Messages () {
        super();
    }

    /**
     * The name of the resource bundle file (without the .properties extension).
     */
    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    /**
     * Cached reference to the loaded resource bundle.
     */
    protected static ResourceBundle bundle = null;

    /**
     * Loads and returns the resource bundle for message lookup.
     * If the bundle is already loaded, returns the cached instance.
     *
     * @return the loaded resource bundle
     */
    private static ResourceBundle getResourceBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        }
        return bundle;
    }

    /**
     * Looks up the message associated with the given key and replaces placeholders
     * with the specified single binding value.
     *
     * @param id      the key of the message in the resource bundle
     * @param binding the value to substitute into the message
     * @return the formatted message, or the key if not found
     */
    public static String bind(String id, String binding) {
        return bind(id, new String[]{binding});
    }

    /**
     * Looks up the message associated with the given key and replaces placeholders
     * with the specified two binding values.
     *
     * @param id       the key of the message in the resource bundle
     * @param binding1 the first value to substitute
     * @param binding2 the second value to substitute
     * @return the formatted message, or the key if not found
     */
    public static String bind(String id, String binding1, String binding2) {
        return bind(id, new String[]{binding1, binding2});
    }

    /**
     * Retrieves a message from the resource bundle by key.
     * Returns the key itself if the message is not found.
     *
     * @param key the message key
     * @return the message string, or the key if not found, or "!key!" if null pointer occurs
     */
    public static String bind(String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!"; //$NON-NLS-1$  //$NON-NLS-2$
        }
    }

    /**
     * Retrieves a message from the resource bundle by key and formats it with the given arguments.
     * Uses {@link java.text.MessageFormat} for substitution.
     *
     * @param key  the message key
     * @param args the array of objects to substitute into the message
     * @return the formatted message, or the key if not found
     */
    public static String bind(String key, Object[] args) {
        try {
            return MessageFormat.format(bind(key), args);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!";  //$NON-NLS-1$  //$NON-NLS-2$
        }
    }
}
