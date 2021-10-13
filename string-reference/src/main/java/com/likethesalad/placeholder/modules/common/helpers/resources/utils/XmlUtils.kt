package com.likethesalad.placeholder.modules.common.helpers.resources.utils

import com.likethesalad.placeholder.modules.common.Constants.Companion.XML_STRING_TAG
import com.likethesalad.tools.resource.api.android.modules.string.StringAndroidResource
import org.w3c.dom.Document
import org.w3c.dom.Element

class XmlUtils {

    companion object {
        private val RAW_VALUES_FILE_REGEX = Regex("^(?!resolved\\.xml)[A-Za-z0-9_]+\\.xml\$")

        fun stringResourceModelToElement(document: Document, stringResourceModel: StringAndroidResource): Element {
            val strElement = document.createElement(XML_STRING_TAG)
            strElement.textContent = stringResourceModel.stringValue()
            for (it in stringResourceModel.attributes().asMap()) {
                strElement.setAttribute(it.key, it.value)
            }
            return strElement
        }

        fun isValidRawXmlFileName(name: String): Boolean {
            return RAW_VALUES_FILE_REGEX.matches(name)
        }
    }
}