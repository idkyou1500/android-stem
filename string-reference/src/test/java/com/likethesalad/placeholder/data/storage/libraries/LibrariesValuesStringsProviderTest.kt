package com.likethesalad.placeholder.data.storage.libraries

import com.google.common.truth.Truth
import com.likethesalad.placeholder.modules.rawStrings.data.helpers.files.ValuesFolderXmlFiles
import com.likethesalad.placeholder.modules.rawStrings.models.ValuesFolderStrings
import com.likethesalad.placeholder.modules.rawStrings.data.libraries.LibrariesFilesProvider
import com.likethesalad.placeholder.modules.rawStrings.data.libraries.LibrariesValuesStringsProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.File

class LibrariesValuesStringsProviderTest {

    private lateinit var librariesFilesProvider: LibrariesFilesProvider
    private lateinit var librariesValuesStringsProvider: LibrariesValuesStringsProvider

    @Before
    fun setUp() {
        librariesFilesProvider = mockk()
        librariesValuesStringsProvider =
            LibrariesValuesStringsProvider(
                librariesFilesProvider
            )
    }

    @Test
    fun `Get values strings for values folder`() {
        verifyValuesStringsForFolder("values", mockk())
        verifyValuesStringsForFolder("values-es", mockk())
        verifyValuesStringsForFolder("values", null)
    }

    private fun verifyValuesStringsForFolder(
        folderName: String,
        parent: ValuesFolderStrings?
    ) {
        val xmlFiles = setOf<File>(mockk(), mockk())
        val expectedValuesXmlFiles =
            ValuesFolderXmlFiles(folderName, xmlFiles)
        every { librariesFilesProvider.getXmlFilesForFolder(folderName) }.returns(xmlFiles)

        val result = librariesValuesStringsProvider.getValuesStringsFor(folderName, parent)

        Truth.assertThat(result).isEqualTo(
            ValuesFolderStrings(
                folderName,
                expectedValuesXmlFiles,
                parent
            )
        )
    }
}