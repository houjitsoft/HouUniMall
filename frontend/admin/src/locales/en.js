import { createMenuMessages } from './menuMessages.js'
import { createVideoStudioMessages } from './videoStudioMessages.js'
import { createConversionGoalsMessages } from './conversionGoalsMessages.js'
import { createOperationGuideMessages } from './operationGuideMessages.js'

export default {
  common: { retry: 'Retry', loading: 'Loading…', planned: 'Planned' },
  header: {
    chooseProject: 'Select a GEO project', openAccountMenu: 'Open account menu', profile: 'Profile',
    settings: 'Settings', logout: 'Log out', loggingOut: 'Logging out…', user: 'User'
  },
  language: { switch: 'Switch language' },
  project: {
    loadFailed: 'Failed to load projects. Please try again.', empty: 'Create or select a GEO project to load the workspace.',
    loadingContext: 'Loading project context…', welcome: 'Welcome to HouGEO', registerWebsite: 'Register your website first',
    onboardingDescription: 'Create your first GEO project and start optimizing your brand for AI search.', name: 'Project name',
    domain: 'Website domain', namePlaceholder: 'Example: Houjiang Official Website', domainPlaceholder: 'Example: houjit.com',
    createNow: 'Create now', nameRequired: 'Enter a project name', createSuccess: 'Project created. Start your GEO optimization journey!',
    unnamed: 'Project {id}', currentPage: 'Current page'
  },
  navigation: { primary: 'Primary navigation', secondary: 'Secondary navigation', openMenu: 'Open menu' },
  menu: createMenuMessages('en'),
  videoStudio: createVideoStudioMessages('en'),
  conversionGoals: createConversionGoalsMessages('en'),
  operationGuide: createOperationGuideMessages('en')
}
