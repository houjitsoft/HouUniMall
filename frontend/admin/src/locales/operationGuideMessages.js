const rows = {
  action: ['操作方法', 'How to use', '操作方法', 'Cómo usar'],
  aria: ['{page}操作方法', 'How to use {page}', '{page}の操作方法', 'Cómo usar {page}'],
  title: ['{page} · 操作方法', '{page} · How to use', '{page} · 操作方法', '{page} · Cómo usar'],
  featureLabel: ['这个页面实现什么功能', 'What this page does', 'このページでできること', 'Qué permite hacer esta página'],
  reference: ['参考说明', 'Reference', '参考情報', 'Referencia'],
  steps: ['按这几步操作', 'Follow these steps', '次の手順で操作', 'Sigue estos pasos'],
  notice: ['小白注意', 'Before you start', 'はじめに', 'Antes de empezar'],
  manual: ['查看完整 HouGEO 操作手册', 'Open the full HouGEO guide', 'HouGEO 操作マニュアルを開く', 'Abrir la guía completa de HouGEO']
}

const conversionGoalRows = {
  feature: ['把官网上用户真正完成的动作（如提交表单、在线咨询、拨打电话、下单）设成可统计目标。配置并接入后，系统会自动接收这些事件，汇总数量、当前值、完成进度和来源，让你知道 GEO 内容有没有带来咨询、留资和成交。', 'Define measurable user actions such as form submissions, inquiries, phone calls, and purchases. After setup, the system receives and groups real events by goal, showing volume, progress, and source so you can see whether GEO content drives business results.', 'フォーム送信、問い合わせ、電話、購入など、ユーザーが実際に行う行動を測定目標として登録します。接続後は実イベントを目標別に集計し、件数、進捗、流入元を表示して、GEO コンテンツが成果につながったか確認できます。', 'Registra acciones reales como envíos de formularios, consultas, llamadas y compras. Tras la configuración, el sistema agrupa los eventos por objetivo y muestra volumen, progreso y origen para comprobar si el contenido GEO genera resultados.'],
  step1: ['先确认右上角是要分析的项目。目标、事件和统计都会记在这个项目里，选错项目会把数据记到错误的品牌下。', 'First confirm the project you want to analyze. Goals, events, and reports are saved under this project, so choosing the wrong one puts data under the wrong brand.', 'まず右上で分析対象のプロジェクトを確認します。目標、イベント、レポートはこのプロジェクトに保存されるため、選択を誤ると別ブランドに記録されます。', 'Primero confirma el proyecto que vas a analizar. Los objetivos, eventos e informes se guardan en él; elegir otro proyecto registra los datos en la marca equivocada.'],
  step2: ['点击“新增目标”，选用户要完成的动作，填写目标值和截止日期；如果接入官网或导入数据，再填写唯一事件代码。保存后，列表会显示这个目标的当前值和完成进度。', 'Click “Add goal”, choose the user action, and enter the target value and deadline. If you connect a website or import data, also enter its unique event code. After saving, the goal shows its current value and progress.', '「目標を追加」をクリックし、ユーザー行動、目標値、期限を入力します。サイト接続またはデータ取込を使う場合は、一意のイベントコードも入力します。保存後、現在値と進捗が表示されます。', 'Haz clic en «Añadir objetivo», elige la acción e introduce el valor objetivo y la fecha límite. Si conectas un sitio o importas datos, añade también el código único del evento. Al guardar verás el valor actual y el progreso.'],
  step3: ['在“接入配置”里填允许采集的官网域名，并按接入方式生成或轮换 JS SDK Token、Webhook 密钥。配置成功后，官网上的真实动作才会被系统接收并归入目标。', 'In “Tracking setup”, enter allowed website domains and generate or rotate the JS SDK token or webhook secret for your integration. Once configured, real website actions can be received and assigned to goals.', '「計測設定」で収集を許可するサイトドメインを入力し、接続方式に応じて JS SDK トークンまたは Webhook シークレットを発行・更新します。設定後、サイトの実行動が目標に集計されます。', 'En «Configuración de seguimiento», introduce los dominios permitidos y genera o rota el token del SDK JS o el secreto del webhook. Después, las acciones reales del sitio se reciben y asignan a los objetivos.'],
  step4: ['接入后到“事件明细”查看收到的事件、来源和归因结果；回到目标列表即可看到事件数量、当前完成值和进度变化，用来判断哪些 GEO 内容真正带来咨询、留资或下单。', 'After integration, open “Event details” to see received events, sources, and attribution. Return to the goal list to track event volume, current value, and progress, and identify which GEO content drives inquiries, leads, or purchases.', '接続後は「イベント詳細」で受信イベント、流入元、アトリビューションを確認します。目標一覧に戻ると件数、現在値、進捗の変化を確認でき、成果につながる GEO コンテンツを判断できます。', 'Tras la integración, abre «Detalles de eventos» para revisar eventos, fuentes y atribución. En la lista de objetivos verás el volumen, el valor actual y el progreso para identificar qué contenido GEO genera consultas, contactos o compras.'],
  tip: ['事件代码相当于目标的唯一名称。官网埋点、Webhook 或 CSV 中的代码必须完全一致；否则系统虽然能收到事件，却无法把它计入这个目标，目标进度也不会增长。', 'The event code is the goal’s unique name. The code in website tracking, webhooks, or CSV files must match exactly; otherwise events may arrive but cannot count toward this goal or its progress.', 'イベントコードは目標の一意な名前です。サイト計測、Webhook、CSV のコードを完全一致させてください。不一致の場合、イベントを受信しても目標に加算されず、進捗も増えません。', 'El código del evento es el nombre único del objetivo. Debe coincidir exactamente en el seguimiento web, los webhooks o los CSV; de lo contrario, el evento puede recibirse pero no contará para el objetivo ni su progreso.']
}

const localeIndex = { 'zh-CN': 0, en: 1, ja: 2, es: 3 }
const select = (source, index) => Object.fromEntries(
  Object.entries(source).map(([key, translations]) => [key, translations[index]])
)

export function createOperationGuideMessages(locale) {
  const index = localeIndex[locale] ?? 0
  return {
    ...select(rows, index),
    pages: {
      conversionGoals: select(conversionGoalRows, index)
    }
  }
}
