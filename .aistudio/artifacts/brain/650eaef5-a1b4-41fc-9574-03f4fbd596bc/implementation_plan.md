# Plano de Implementação: Configuração Obrigatória Pré-Sessão e Registro Manual de Estudos

Este plano detalha a reestruturação do fluxo do Pomodoro e a inclusão do registro manual de sessões, garantindo que o usuário defina obrigatoriamente o assunto, o vínculo automático com a disciplina e o tipo de estudo antes de iniciar o timer, além de possibilitar registros manuais retroativos no banco de dados SQLite local (Room).

---

## 1. Visão Geral das Mudanças

1. **Remoção do Seletor Solto no Topo do Timer:**
   - A barra superior do Pomodoro não terá mais o seletor genérico de disciplina solto.
   - O estado do timer antes do início será "Aguardando configuração de estudo".
2. **Fluxo Obrigatório Pré-Sessão (Configuração antes de iniciar):**
   - Ao tocar em **Iniciar (Play)**, se a sessão ainda não tiver sido configurada, abre-se um modal dedicado:
     - **Assunto (Tópico):** Campo de texto com sugestões/autocompletar dos assuntos já estudados.
     - **Vínculo Automático com Matéria:** Ao digitar ou escolher um assunto já cadastrado anteriormente, o app identifica e seleciona automaticamente a matéria correspondente. Se for um assunto novo, o usuário escolhe a matéria à qual ele pertence.
     - **Tipo de Estudo:** Seletor visual obrigatório entre 4 modalidades:
       - 📖 **Leitura**
       - 📝 **Questões**
       - 🎥 **Vídeo**
       - 🔄 **Revisão**
     - Botão **"Iniciar Estudo"**: Valida os dados, salva o mapeamento assunto-matéria e inicia o cronômetro do Pomodoro.
   - Durante a contagem, o display exibe em destaque o **Assunto**, a **Matéria** vinculada (com tag e cor) e o **Tipo de Estudo**.
3. **Registro Manual de Estudos (Sessões Retroativas):**
   - Novo botão de ação no Histórico ("+ Registrar Estudo Manual").
   - Formulário para registrar:
     - Duração (em minutos ou horas).
     - Data e horário da sessão.
     - Assunto (com o mesmo vínculo automático à matéria).
     - Matéria (auto-preenchida ou selecionável se for assunto novo).
     - Tipo de estudo (Leitura, Questões, Vídeo, Revisão).
     - Anotações opcionais.
   - Salva diretamente no Room SQLite e atualiza os gráficos analíticos instantaneamente.

---

## 2. Modelagem de Dados & Room Database

### 2.1. Nova Entidade: `TopicEntity` (Mapeamento Assunto ➔ Matéria)
Para garantir que futuros registros do mesmo assunto vinculem a matéria automaticamente:

```kotlin
@Entity(
    tableName = "topics",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["subject_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subjectId: Long,
    val lastUsedEpochMs: Long = System.currentTimeMillis()
)
```

### 2.2. Atualização da Entidade `StudySession`
Adicionar os campos:
- `topic`: Nome do assunto estudado (ex: "Equações Diferenciais", "Revolução Francesa").
- `studyType`: Modalidade ("READING", "EXERCISES", "VIDEO", "REVIEW").
- Incrementar Room Database para `version = 3`.

```kotlin
@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["start_time"]),
        Index(value = ["subject_id", "start_time"]),
        Index(value = ["topic", "start_time"]),
        Index(value = ["study_type", "start_time"]),
        Index(value = ["completed", "start_time"])
    ]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "category") val category: String = "FOCUS",
    @ColumnInfo(name = "total_duration") val totalDuration: Long,
    @ColumnInfo(name = "subject_id") val subjectId: Long?,
    @ColumnInfo(name = "topic") val topic: String = "",
    @ColumnInfo(name = "study_type") val studyType: String = "READING",
    @ColumnInfo(name = "completed") val completed: Boolean = true,
    @ColumnInfo(name = "notes") val notes: String = ""
)
```

---

## 3. Fluxo de Interação & Telas

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        TELA DO POMODORO                                │
│                                                                        │
│   [ Botão Play pressionado ]                                          │
│              │                                                         │
│              ▼                                                         │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │   MODAL: CONFIGURAÇÃO OBRIGATÓRIA DA SESSÃO                  │     │
│   │                                                              │     │
│   │   1. Assunto: [ Digite ou selecione histórico ]              │     │
│   │   2. Matéria: [ Vinculada automaticamente se assunto existe ]│     │
│   │               (ou seletor de matéria se assunto for novo)    │     │
│   │   3. Tipo de Estudo:                                         │     │
│   │      [ 📖 Leitura ]  [ 📝 Questões ]  [ 🎥 Vídeo ]  [ 🔄 Rev ]│    │
│   │                                                              │     │
│   │   [ Iniciar Foco (25m) ]                                     │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  │                                     │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │   TIMER EM EXECUÇÃO:                                         │     │
│   │   - Assunto: "Cálculo I - Derivadas"                         │     │
│   │   - Matéria: Matemática (Tag Azul)                           │     │
│   │   - Tipo: 📝 Questões                                        │     │
│   │   - 24:59 restante                                           │     │
│   └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                        TELA DE HISTÓRICO                               │
│                                                                        │
│   [ + Registrar Estudo Manual ] ──► Abre Modal de Registro Manual      │
│   - Informa tempo (ex: 45 min)                                         │
│   - Informa Assunto, Matéria (auto-vinculada) e Tipo de Estudo         │
│   - Grava sessão retroativa no Room                                    │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Passos de Implementação (Após Aprovação)

1. **Modelagem & Banco de Dados:**
   - Criar `TopicEntity` e `TopicDao` (para buscar assunto por nome e sugerir autocompletar).
   - Atualizar `StudySession` com campos `topic` e `studyType`.
   - Atualizar DAOs e repositório com busca inteligente: `getSubjectForTopic(topicName)`.
   - Incrementar versão do Room para 3.
2. **Atualização do Pomodoro:**
   - Criar componente de diálogo modal obrigatório pré-sessão.
   - Atualizar `PomodoroViewModel` para exigir configuração ativa antes de iniciar a contagem.
   - Exibir no mostrador os metadados da sessão em andamento.
3. **Registro Manual de Estudos:**
   - Criar diálogo modal "Registrar Estudo Manual" acessível no Histórico.
   - Atualizar `HistoryViewModel` com método `addManualSession(...)`.
   - Renderizar no cartão de histórico os badges de tipo de estudo (📖, 📝, 🎥, 🔄) e assunto.
4. **Testes de Regressão Automatizados:**
   - Testar o mapeamento automático assunto ➔ matéria.
   - Testar gravação de sessões manuais e consistência de dados analíticos.
   - Rodar `compile_applet` e `gradle :app:testDebugUnitTest`.
