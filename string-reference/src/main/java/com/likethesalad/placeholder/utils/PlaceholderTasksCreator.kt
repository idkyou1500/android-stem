package com.likethesalad.placeholder.utils

import com.likethesalad.android.templates.common.tasks.identifier.TemplatesIdentifierTask
import com.likethesalad.android.templates.common.tasks.identifier.data.TemplateItemsSerializer
import com.likethesalad.placeholder.ResolvePlaceholdersPlugin
import com.likethesalad.placeholder.configuration.ResolvePlaceholderConfiguration
import com.likethesalad.placeholder.locator.listener.TypeLocatorCreationListener
import com.likethesalad.placeholder.modules.common.helpers.android.AndroidVariantContext
import com.likethesalad.placeholder.modules.common.models.TasksNamesModel
import com.likethesalad.placeholder.modules.resolveStrings.ResolvePlaceholdersTask
import com.likethesalad.placeholder.modules.resolveStrings.data.ResolvePlaceholdersArgs
import com.likethesalad.placeholder.modules.templateStrings.GatherTemplatesTask
import com.likethesalad.placeholder.modules.templateStrings.data.GatherTemplatesArgs
import com.likethesalad.placeholder.providers.TaskContainerProvider
import com.likethesalad.tools.resource.collector.android.data.variant.VariantTree
import com.likethesalad.tools.resource.locator.android.extension.configuration.data.ResourceLocatorInfo
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UnstableApiUsage")
@Singleton
class PlaceholderTasksCreator @Inject constructor(
    taskContainerProvider: TaskContainerProvider,
    private val androidVariantContextFactory: AndroidVariantContext.Factory,
    private val taskActionProviderHolder: TaskActionProviderHolder,
    private val templateItemsSerializer: TemplateItemsSerializer,
    private val configuration: ResolvePlaceholderConfiguration
) : TypeLocatorCreationListener.Callback {

    companion object {
        const val RESOURCE_TYPE_COMMON = "common"
        const val RESOURCE_TYPE_TEMPLATE = "template"
    }

    private val taskContainer by lazy { taskContainerProvider.getTaskContainer() }

    override fun onLocatorsReady(variantTree: VariantTree, locatorsByType: Map<String, ResourceLocatorInfo>) {
        val androidVariantContext = androidVariantContextFactory.create(variantTree)
        val commonResourcesInfo = locatorsByType.getValue(RESOURCE_TYPE_COMMON)
        val templateResourcesInfo = locatorsByType.getValue(RESOURCE_TYPE_TEMPLATE)
        createResolvePlaceholdersTaskForVariant(androidVariantContext, commonResourcesInfo, templateResourcesInfo)
    }

    private fun createResolvePlaceholdersTaskForVariant(
        androidVariantContext: AndroidVariantContext,
        commonResourcesInfo: ResourceLocatorInfo,
        templateResourcesInfo: ResourceLocatorInfo
    ) {
        val gatherTemplatesActionProvider = taskActionProviderHolder.gatherTemplatesActionProvider
        val resolvePlaceholdersActionProvider = taskActionProviderHolder.resolvePlaceholdersActionProvider

        val templatesIdentifierTask = createTemplatesIdentifierTaskProvider(
            androidVariantContext.tasksNames,
            templateResourcesInfo
        )

        val gatherTemplatesTask = taskContainer.register(
            androidVariantContext.tasksNames.gatherStringTemplatesName,
            GatherTemplatesTask::class.java,
            GatherTemplatesArgs(
                gatherTemplatesActionProvider.provide(androidVariantContext),
                commonResourcesInfo.resourcesProvider
            )
        )

        gatherTemplatesTask.configure {
            it.group = ResolvePlaceholdersPlugin.RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.commonResourcesDir.set(commonResourcesInfo.taskInfo.outputDirectoryProvider.getOutputDirProperty())
            it.templateIdsFile.set(templatesIdentifierTask.flatMap { identifierTask -> identifierTask.outputFile })
        }

        val resolvePlaceholdersTask = taskContainer.register(
            androidVariantContext.tasksNames.resolvePlaceholdersName,
            ResolvePlaceholdersTask::class.java,
            ResolvePlaceholdersArgs(resolvePlaceholdersActionProvider.provide(androidVariantContext))
        )

        resolvePlaceholdersTask.configure {
            it.group = ResolvePlaceholdersPlugin.RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.templatesDir.set(gatherTemplatesTask.flatMap { gatherTemplates -> gatherTemplates.outDir })
            it.outputDir.set(androidVariantContext.variantBuildResolvedDir.resolvedDir)
        }

        if (configuration.resolveOnBuild()) {
            androidVariantContext.mergeResourcesTask.dependsOn(resolvePlaceholdersTask)
        }
    }

    private fun createTemplatesIdentifierTaskProvider(
        taskNames: TasksNamesModel,
        localResourcesInfo: ResourceLocatorInfo
    ): TaskProvider<TemplatesIdentifierTask> {
        val provider = taskContainer.register(
            taskNames.templatesIdentifierName,
            TemplatesIdentifierTask::class.java,
            TemplatesIdentifierTask.Args(
                localResourcesInfo.resourcesProvider,
                templateItemsSerializer
            )
        )

        provider.configure {
            it.group = ResolvePlaceholdersPlugin.RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.localResourcesDir.set(localResourcesInfo.taskInfo.outputDirectoryProvider.getOutputDirProperty())
        }

        return provider
    }
}