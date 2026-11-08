import { createMenuMessages } from './menuMessages.js'
import { createVideoStudioMessages } from './videoStudioMessages.js'
import { createConversionGoalsMessages } from './conversionGoalsMessages.js'
import { createOperationGuideMessages } from './operationGuideMessages.js'

export default {
  common: { retry: '再試行', loading: '読み込み中…', planned: '開発予定' },
  header: {
    chooseProject: 'GEO プロジェクトを選択', openAccountMenu: 'アカウントメニューを開く', profile: 'プロフィール',
    settings: '設定', logout: 'ログアウト', loggingOut: 'ログアウト中…', user: 'ユーザー'
  },
  language: { switch: '言語を切り替える' },
  project: {
    loadFailed: 'プロジェクトを読み込めませんでした。再試行してください。', empty: 'ワークスペースを開くには GEO プロジェクトを作成または選択してください。',
    loadingContext: 'プロジェクト情報を読み込み中…', welcome: 'HouGEO へようこそ', registerWebsite: '最初にウェブサイトを登録してください',
    onboardingDescription: '最初の GEO プロジェクトを作成し、ブランドの AI 検索最適化を始めましょう。', name: 'プロジェクト名',
    domain: 'ウェブサイトのドメイン', namePlaceholder: '例：Houjiang 公式サイト', domainPlaceholder: '例：houjit.com',
    createNow: '今すぐ作成', nameRequired: 'プロジェクト名を入力してください', createSuccess: 'プロジェクトを作成しました。GEO 最適化を始めましょう！',
    unnamed: 'プロジェクト {id}', currentPage: '現在のページ'
  },
  navigation: { primary: 'メインナビゲーション', secondary: 'サブナビゲーション', openMenu: 'メニューを開く' },
  menu: createMenuMessages('ja'),
  videoStudio: createVideoStudioMessages('ja'),
  conversionGoals: createConversionGoalsMessages('ja'),
  operationGuide: createOperationGuideMessages('ja')
}
