package com.likethesalad.placeholder.modules.rawStrings.data

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.likethesalad.placeholder.modules.common.helpers.dirs.VariantDirsPathFinder
import com.likethesalad.placeholder.modules.rawStrings.data.helpers.dirs.VariantValuesFolders
import com.likethesalad.placeholder.modules.common.helpers.dirs.ValuesFoldersExtractor
import com.likethesalad.placeholder.modules.rawStrings.data.libraries.LibrariesValuesStringsProvider
import com.likethesalad.placeholder.modules.rawStrings.models.ValuesFolderStrings
import com.likethesalad.placeholder.modules.rawStrings.models.VariantXmlFiles
import java.io.File

@AutoFactory
class VariantRawStrings(
    private val variantDirsPathFinder: VariantDirsPathFinder,
    private val librariesValuesStringsProvider: LibrariesValuesStringsProvider,
    @Provided private val valuesFolderStringsProvider: ValuesFolderStringsProvider
) {

    companion object {
        private const val BASE_VALUES_FOLDER_NAME = "values"
    }

    fun getValuesStrings(generatedResDirs: Set<File>): Collection<ValuesFolderStrings> {
        val variantStringFilesList = getVariantStringFilesList(generatedResDirs)
        val uniqueValuesFolderNames = getUniqueValuesFolderNames(variantStringFilesList.map {
            it.valuesXmlFiles.keys
        })

        val valuesStringsPerFolder = getValuesStringsMapPerFolder(
            uniqueValuesFolderNames.filter { it != BASE_VALUES_FOLDER_NAME },
            variantStringFilesList
        )

        return valuesStringsPerFolder.values
    }

    private fun getValuesStringsMapPerFolder(
        uniqueValuesFolderNames: List<String>,
        variantXmlFilesList: List<VariantXmlFiles>
    ): Map<String, ValuesFolderStrings> {
        val valuesStringsPerFolder = mutableMapOf<String, ValuesFolderStrings>()

        val baseValuesStrings = valuesFolderStringsProvider.getValuesStringsForFolderFromVariants(
            BASE_VALUES_FOLDER_NAME,
            variantXmlFilesList,
            librariesValuesStringsProvider.getValuesStringsFor(BASE_VALUES_FOLDER_NAME, null)
        )
        if (baseValuesStrings != null) {
            valuesStringsPerFolder[BASE_VALUES_FOLDER_NAME] = baseValuesStrings
        }

        for (valuesFolderName in uniqueValuesFolderNames) {
            val valuesFolderStrings: ValuesFolderStrings? = valuesFolderStringsProvider.getValuesStringsForFolderFromVariants(
                valuesFolderName,
                variantXmlFilesList,
                librariesValuesStringsProvider.getValuesStringsFor(valuesFolderName, baseValuesStrings)
            )

            if (valuesFolderStrings != null) {
                valuesStringsPerFolder[valuesFolderName] = valuesFolderStrings
            }
        }

        return valuesStringsPerFolder
    }

    private fun getUniqueValuesFolderNames(valuesFolderNames: List<Set<String>>): Set<String> {
        return valuesFolderNames.flatten().toSet()
    }

    private fun getVariantStringFilesList(generatedResDir: Set<File>): List<VariantXmlFiles> {
        val dirsPaths = variantDirsPathFinder
            .getExistingPathsResDirs(getExtraMainResDirs(generatedResDir))
        val variantValuesFoldersList =
            dirsPaths.map {
                VariantValuesFolders(
                    it.variantName,
                    ValuesFoldersExtractor(
                        it.paths
                    )
                )
            }
        return variantValuesFoldersList.map {
            VariantXmlFiles(
                it.variantName,
                it.valuesXmlFiles
            )
        }
    }

    private fun getExtraMainResDirs(generatedResDirs: Set<File>): List<File>? {
        val existingNotEmptyDirs = generatedResDirs.filter {
            it.exists() && it.listFiles()?.isNotEmpty() == true
        }

        if (existingNotEmptyDirs.isNotEmpty()) {
            return existingNotEmptyDirs
        }

        return null
    }

}
