import { createMenuMessages } from './menuMessages.js'
import { createVideoStudioMessages } from './videoStudioMessages.js'
import { createConversionGoalsMessages } from './conversionGoalsMessages.js'
import { createOperationGuideMessages } from './operationGuideMessages.js'

export default {
  common: { retry: 'Reintentar', loading: 'Cargando…', planned: 'Planificado' },
  header: {
    chooseProject: 'Seleccionar un proyecto GEO', openAccountMenu: 'Abrir menú de cuenta', profile: 'Perfil',
    settings: 'Configuración', logout: 'Cerrar sesión', loggingOut: 'Cerrando sesión…', user: 'Usuario'
  },
  language: { switch: 'Cambiar idioma' },
  project: {
    loadFailed: 'No se pudieron cargar los proyectos. Inténtalo de nuevo.', empty: 'Crea o selecciona un proyecto GEO para cargar el espacio de trabajo.',
    loadingContext: 'Cargando el contexto del proyecto…', welcome: 'Te damos la bienvenida a HouGEO', registerWebsite: 'Registra primero tu sitio web',
    onboardingDescription: 'Crea tu primer proyecto GEO y empieza a optimizar tu marca para la búsqueda con IA.', name: 'Nombre del proyecto',
    domain: 'Dominio del sitio web', namePlaceholder: 'Ejemplo: sitio oficial de Houjiang', domainPlaceholder: 'Ejemplo: houjit.com',
    createNow: 'Crear ahora', nameRequired: 'Introduce el nombre del proyecto', createSuccess: 'Proyecto creado. ¡Empieza tu optimización GEO!',
    unnamed: 'Proyecto {id}', currentPage: 'Página actual'
  },
  navigation: { primary: 'Navegación principal', secondary: 'Navegación secundaria', openMenu: 'Abrir menú' },
  menu: createMenuMessages('es'),
  videoStudio: createVideoStudioMessages('es'),
  conversionGoals: createConversionGoalsMessages('es'),
  operationGuide: createOperationGuideMessages('es')
}
