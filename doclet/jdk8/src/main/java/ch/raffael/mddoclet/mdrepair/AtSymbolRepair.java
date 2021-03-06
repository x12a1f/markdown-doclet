/*
 * Copyright 2013-2016 Raffael Herzog, Marko Umek
 *
 * This file is part of markdown-doclet.
 *
 * markdown-doclet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * markdown-doclet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with markdown-doclet.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package ch.raffael.mddoclet.mdrepair;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AtSymbolRepair allows to use the '@' symbol in external JavaDoc fragments (like
 * the Gist Taglet) without running into any issues with JavaDoc.
 *
 * **Important Note**: This is only useful when importing fragments that may contain
 * '@' from external sources. If an '@' is in the actual JavaDoc source code, it will
 * be interpreted by JavaDoc before this class gets a chance to fix anything.
 *
 * {@link UnescapeAtSymbolRepair} is used for providing an escape for the JavaDoc sources.
 * The two classes don't clash, i.e. in external sources, there's no need to escape
 * the '@' symbol, escaping actually won't even work with those.
 */
final class AtSymbolRepair extends DefaultMarkdownRepair {
    static final String MARKER = "{-at-}";
    static final String AT_HTML_ENTITY = "&#64;";

    private static final Pattern SUBST_REGEX =Pattern.compile("@|\\{-at-\\}");
    private static final Pattern RESTORE_REGEX =Pattern.compile("\\{-at-\\}");

    private final List<String> storage;

    AtSymbolRepair() {
        this(new LinkedList<String>());
    }

    AtSymbolRepair(List<String> storage) {
        this.storage = storage;
    }

    @Override
    public String beforeMarkdownParser(String markdown) {
        final StringBuffer result=new StringBuffer();
        final Matcher matcher= SUBST_REGEX.matcher(markdown);
        while ( matcher.find() )  {
            storeValue(matcher.group());
            matcher.appendReplacement(result, MARKER);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    private void storeValue(String value) {
        // if the markdown already contains the MARKER
        if( MARKER.equals(value) ) {
            storage.add(MARKER);
        }
        else {
            storage.add(AT_HTML_ENTITY);
        }
    }

    @Override
    public String afterMarkdownParser(String markup) {
        final StringBuffer result=new StringBuffer();
        final Matcher matcher= RESTORE_REGEX.matcher(markup);
        while ( matcher.find() )  {
            String replacement=MARKER;
            if( ! storage.isEmpty() ) {
                replacement=storage.remove(0);
            }

            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
