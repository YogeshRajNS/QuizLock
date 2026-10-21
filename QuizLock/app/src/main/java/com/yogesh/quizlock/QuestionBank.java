package com.yogesh.quizlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Multiple-choice questions on RAG, GenAI and Agentic AI. Add your own in build(). */
public final class QuestionBank {

    public static final class Question {
        public final String topic;
        public final String text;
        public final String[] options;
        public final int answer;      // index into options
        public final String why;

        Question(String topic, String text, int answer, String why, String... options) {
            this.topic = topic;
            this.text = text;
            this.options = options;
            this.answer = answer;
            this.why = why;
        }

        /** Same question with options in random order (so you can't memorise "always B"). */
        Question shuffled() {
            List<Integer> idx = new ArrayList<>();
            for (int i = 0; i < options.length; i++) idx.add(i);
            Collections.shuffle(idx);
            String[] opts = new String[options.length];
            int newAns = 0;
            for (int i = 0; i < idx.size(); i++) {
                opts[i] = options[idx.get(i)];
                if (idx.get(i) == answer) newAns = i;
            }
            return new Question(topic, text, newAns, why, opts);
        }
    }

    private static final List<Question> ALL = build();

    private QuestionBank() {}

    /** Picks n random questions, trying to mix all three topics. */
    public static List<Question> pick(int n) {
        List<Question> pool = new ArrayList<>(ALL);
        Collections.shuffle(pool);
        List<Question> out = new ArrayList<>();
        String[] topics = {"RAG", "GenAI", "Agentic AI"};
        for (String t : topics) {
            for (Question q : pool) {
                if (q.topic.equals(t) && !out.contains(q)) { out.add(q); break; }
            }
        }
        for (Question q : pool) {
            if (out.size() >= n) break;
            if (!out.contains(q)) out.add(q);
        }
        Collections.shuffle(out);
        List<Question> result = new ArrayList<>();
        for (Question q : out.subList(0, Math.min(n, out.size()))) result.add(q.shuffled());
        return result;
    }

    private static List<Question> build() {
        List<Question> q = new ArrayList<>();
        String R = "RAG", G = "GenAI", A = "Agentic AI";

        // ---------------- RAG ----------------
        q.add(new Question(R, "What is the main purpose of Retrieval-Augmented Generation (RAG)?", 0,
                "RAG fetches relevant external documents at query time and puts them in the prompt, grounding answers without retraining.",
                "Ground LLM answers in external documents retrieved at query time",
                "Increase the number of model parameters",
                "Compress the model for mobile devices",
                "Replace tokenization with embeddings"));
        q.add(new Question(R, "In a RAG pipeline, what are embeddings mainly used for?", 1,
                "Embeddings map text to vectors so semantically similar chunks are close together and can be found by similarity search.",
                "Encrypting documents before storage",
                "Representing text as vectors for semantic similarity search",
                "Counting tokens in the prompt",
                "Fine-tuning the LLM weights"));
        q.add(new Question(R, "Which similarity metric is most commonly used to compare text embeddings?", 2,
                "Cosine similarity compares vector direction and is the default for most text-embedding search.",
                "Hamming distance", "Jaccard index on characters", "Cosine similarity", "Levenshtein distance"));
        q.add(new Question(R, "Why do we split documents into chunks before indexing?", 0,
                "Smaller chunks fit the context window and give more precise retrieval than whole documents.",
                "To fit context limits and improve retrieval precision",
                "Because vector databases only accept 10 words",
                "To make the LLM train faster",
                "To remove stop words"));
        q.add(new Question(R, "What is the purpose of chunk overlap?", 3,
                "Overlap keeps sentences that straddle a boundary from losing their context in both chunks.",
                "To double the index size for redundancy",
                "To speed up embedding generation",
                "To reduce hallucinations to zero",
                "To preserve context that crosses chunk boundaries"));
        q.add(new Question(R, "What does a re-ranker (cross-encoder) do in RAG?", 1,
                "A cross-encoder scores each (query, chunk) pair jointly and reorders the first-stage candidates more accurately.",
                "Generates the final answer",
                "Re-scores retrieved candidates jointly with the query for better ordering",
                "Creates the embeddings for indexing",
                "Splits documents into chunks"));
        q.add(new Question(R, "Hybrid search in RAG usually combines:", 2,
                "Hybrid search merges keyword (BM25) and dense vector results, often with Reciprocal Rank Fusion.",
                "Two different LLMs",
                "GPU and CPU inference",
                "Keyword search (e.g. BM25) and dense vector search",
                "SQL and NoSQL databases"));
        q.add(new Question(R, "Which index type is commonly used for Approximate Nearest Neighbour search in vector DBs like Milvus?", 0,
                "HNSW is a graph-based ANN index offering fast, high-recall search.",
                "HNSW", "B-Tree", "Hash join", "Inverted bitmap only"));
        q.add(new Question(R, "In HyDE (Hypothetical Document Embeddings), what is embedded for retrieval?", 3,
                "HyDE asks the LLM to write a hypothetical answer, then embeds that answer to search for real documents.",
                "Only the document titles",
                "The user's raw query characters",
                "The final generated answer after retrieval",
                "A hypothetical answer generated by the LLM from the query"));
        q.add(new Question(R, "The 'lost in the middle' problem means:", 1,
                "LLMs tend to use information at the start and end of long contexts better than information in the middle.",
                "Vector DBs lose middle chunks during indexing",
                "LLMs under-use relevant info placed in the middle of a long context",
                "Embeddings drop the middle tokens",
                "Re-rankers ignore middle-ranked documents"));
        q.add(new Question(R, "In RAGAS evaluation, 'faithfulness' measures:", 0,
                "Faithfulness checks whether the answer's claims are supported by the retrieved context.",
                "Whether the answer is supported by the retrieved context",
                "How fast the retriever responds",
                "Whether the query is grammatically correct",
                "How many chunks are in the index"));
        q.add(new Question(R, "In RAGAS, 'context recall' measures:", 2,
                "Context recall checks whether the retrieved context contains the information needed for the ground-truth answer.",
                "How short the retrieved chunks are",
                "How many times the LLM is called",
                "Whether retrieved context covers the info needed for the ground-truth answer",
                "Latency of the embedding model"));
        q.add(new Question(R, "Multi-query retrieval improves recall by:", 3,
                "The LLM rewrites the question several ways, retrieves for each, and merges results.",
                "Using multiple GPUs",
                "Storing each chunk multiple times",
                "Asking the user several follow-up questions",
                "Generating several rephrasings of the query and merging their results"));
        q.add(new Question(R, "Parent-document (small-to-big) retrieval works by:", 0,
                "Small chunks are matched precisely, then their larger parent section is passed to the LLM for full context.",
                "Retrieving small chunks but sending their larger parent section to the LLM",
                "Only indexing the document's title",
                "Sending the whole corpus to the LLM",
                "Using a parent LLM to supervise a child LLM"));
        q.add(new Question(R, "Metadata filtering in a vector database is used to:", 1,
                "Filters like date, source or tenant narrow the search space before or during similarity search.",
                "Compress the embeddings",
                "Restrict search to chunks matching attributes like source, date or user",
                "Generate new metadata with an LLM",
                "Replace the embedding model"));
        q.add(new Question(R, "GraphRAG differs from basic RAG mainly by:", 2,
                "GraphRAG builds an entity/relationship graph (often with community summaries) to answer multi-hop and global questions.",
                "Using images instead of text",
                "Skipping retrieval entirely",
                "Using a knowledge graph of entities and relations for retrieval",
                "Only working with SQL databases"));

        // ---------------- GenAI / LLM ----------------
        q.add(new Question(G, "What does 'temperature' control in LLM text generation?", 0,
                "Higher temperature flattens the probability distribution, giving more random outputs; lower is more deterministic.",
                "Randomness of token sampling",
                "Maximum context length",
                "GPU heat limit",
                "Number of attention heads"));
        q.add(new Question(G, "Top-p (nucleus) sampling selects the next token from:", 3,
                "Top-p keeps the smallest set of tokens whose cumulative probability reaches p, then samples from them.",
                "Exactly the top 1 token",
                "All tokens uniformly",
                "The p least likely tokens",
                "The smallest set of tokens whose cumulative probability ≥ p"));
        q.add(new Question(G, "The core mechanism of the Transformer architecture is:", 1,
                "Self-attention lets every token weigh every other token in the sequence.",
                "Recurrent loops", "Self-attention", "Convolution only", "Decision trees"));
        q.add(new Question(G, "What does LoRA do during fine-tuning?", 2,
                "LoRA freezes base weights and trains small low-rank adapter matrices, cutting trainable parameters drastically.",
                "Retrains all model weights from scratch",
                "Removes layers from the model",
                "Trains small low-rank adapter matrices while freezing base weights",
                "Converts the model to a vector database"));
        q.add(new Question(G, "QLoRA adds what on top of LoRA?", 0,
                "QLoRA loads the frozen base model in 4-bit quantization and trains LoRA adapters on top, saving memory.",
                "4-bit quantization of the frozen base model",
                "A second, larger model",
                "Reinforcement learning from human feedback",
                "Retrieval at training time"));
        q.add(new Question(G, "An LLM 'hallucination' is:", 3,
                "Hallucination is fluent output that is factually wrong or unsupported by the source.",
                "A tokenizer error",
                "A GPU memory overflow",
                "When the model refuses to answer",
                "Confident output that is false or unsupported"));
        q.add(new Question(G, "What is the context window of an LLM?", 1,
                "It's the maximum number of tokens (prompt + output) the model can attend to at once.",
                "The number of users it can serve",
                "The maximum number of tokens it can process at once",
                "The training dataset size",
                "The size of the vocabulary"));
        q.add(new Question(G, "Chain-of-Thought prompting encourages the model to:", 2,
                "CoT asks the model to reason step by step, which improves multi-step reasoning tasks.",
                "Answer with a single word",
                "Call external tools",
                "Write out intermediate reasoning steps",
                "Ignore the system prompt"));
        q.add(new Question(G, "Few-shot prompting means:", 0,
                "Few-shot prompting includes a handful of input/output examples in the prompt to show the desired pattern.",
                "Giving a few examples of the task in the prompt",
                "Training the model for a few epochs",
                "Limiting the answer to a few tokens",
                "Using a few GPUs"));
        q.add(new Question(G, "RLHF stands for:", 3,
                "Reinforcement Learning from Human Feedback trains a reward model on human preferences and optimises the LLM against it.",
                "Recursive Language Hierarchical Fine-tuning",
                "Retrieval Layer for High Fidelity",
                "Randomised Learning with Hidden Features",
                "Reinforcement Learning from Human Feedback"));
        q.add(new Question(G, "DPO (Direct Preference Optimization) differs from classic RLHF because it:", 1,
                "DPO optimises the policy directly on preference pairs, removing the separate reward model and RL loop.",
                "Needs no preference data at all",
                "Skips the separate reward model and RL step",
                "Only works for image models",
                "Uses retrieval during training"));
        q.add(new Question(G, "What does the KV cache speed up?", 2,
                "Caching attention keys/values of previous tokens avoids recomputing them for every new token.",
                "Tokenization",
                "Embedding database lookups",
                "Autoregressive decoding by reusing past keys/values",
                "Model download time"));
        q.add(new Question(G, "Prompt injection is:", 0,
                "Prompt injection hides instructions in user input or retrieved content to override the system's intended behaviour.",
                "Malicious instructions in input/content that hijack the model's behaviour",
                "Adding more examples to a prompt",
                "Injecting embeddings into the vector DB",
                "A method to compress prompts"));
        q.add(new Question(G, "Byte Pair Encoding (BPE) is used for:", 3,
                "BPE builds a subword vocabulary by merging frequent character pairs, used by many LLM tokenizers.",
                "Model quantization",
                "Vector indexing",
                "Image generation",
                "Subword tokenization"));
        q.add(new Question(G, "Quantizing a model (e.g. to INT8/4-bit) mainly:", 1,
                "Lower-precision weights cut memory and speed up inference with some accuracy trade-off.",
                "Increases its accuracy",
                "Reduces memory and speeds up inference",
                "Adds more parameters",
                "Enables retrieval"));
        q.add(new Question(G, "Mixture of Experts (MoE) models are efficient because:", 0,
                "A router activates only a few expert sub-networks per token, so compute per token is much less than total parameters.",
                "Only a few experts are activated per token",
                "They have no attention layers",
                "They skip tokenization",
                "They are trained without data"));

        // ---------------- Agentic AI ----------------
        q.add(new Question(A, "The ReAct pattern for agents interleaves:", 2,
                "ReAct alternates Reasoning (thoughts) and Acting (tool calls), feeding observations back in.",
                "Retrieval and Caching",
                "Read and Activate memory",
                "Reasoning steps and tool actions with observations",
                "React.js UI and API calls"));
        q.add(new Question(A, "What is 'tool calling' (function calling) in an LLM agent?", 0,
                "The model outputs a structured call (name + arguments) that the app executes and returns results for.",
                "The model emits a structured request to run an external function",
                "The model calls the user on the phone",
                "Compiling the model into a tool",
                "Fine-tuning on tool manuals"));
        q.add(new Question(A, "What is MCP (Model Context Protocol)?", 3,
                "MCP is an open protocol (introduced by Anthropic) for connecting LLM apps to tools and data sources in a standard way.",
                "A GPU memory compression protocol",
                "A model compression technique",
                "A multi-cloud payment protocol",
                "An open standard for connecting AI apps to tools and data sources"));
        q.add(new Question(A, "Short-term memory in an agent is typically:", 1,
                "Short-term memory is the running conversation/scratchpad kept in the context window.",
                "A fine-tuned model checkpoint",
                "The current context window / conversation history",
                "A permanent SQL database",
                "The tokenizer vocabulary"));
        q.add(new Question(A, "Long-term memory for an agent is commonly implemented with:", 2,
                "Agents store facts/episodes externally, often in a vector store, and retrieve them when relevant.",
                "A larger temperature",
                "More attention heads",
                "An external store such as a vector database",
                "A longer system prompt only"));
        q.add(new Question(A, "In a supervisor (orchestrator–worker) multi-agent design:", 0,
                "A supervisor agent decomposes the task and routes sub-tasks to specialised worker agents.",
                "A central agent delegates sub-tasks to specialised worker agents",
                "All agents vote on every token",
                "Agents never communicate",
                "One agent is trained on another's outputs"));
        q.add(new Question(A, "Why add a 'human-in-the-loop' step to an agent?", 3,
                "Human approval before irreversible or high-risk actions limits damage from agent errors.",
                "To make it faster",
                "To reduce token count",
                "To avoid using tools",
                "To approve risky or irreversible actions before execution"));
        q.add(new Question(A, "In LangGraph, an agent workflow is modelled as:", 1,
                "LangGraph represents workflows as a stateful graph of nodes and edges, supporting cycles and checkpoints.",
                "A single linear prompt",
                "A stateful graph of nodes and edges",
                "A relational database schema",
                "A decision tree classifier"));
        q.add(new Question(A, "Agentic RAG differs from basic RAG because the agent:", 2,
                "The agent decides whether/what/where to retrieve, can rewrite queries and iterate until it has enough context.",
                "Never retrieves documents",
                "Uses only one fixed retrieval step",
                "Decides when and how to retrieve, and can iterate",
                "Only works offline"));
        q.add(new Question(A, "The 'Reflection' (self-critique) pattern means the agent:", 0,
                "The agent reviews its own output, critiques it, and revises before returning.",
                "Critiques and revises its own output",
                "Copies another agent's answer",
                "Mirrors the user's tone",
                "Reflects the prompt back unchanged"));
        q.add(new Question(A, "Plan-and-Execute agents differ from pure ReAct agents by:", 3,
                "They first draft a multi-step plan, then execute steps (possibly re-planning), instead of deciding one step at a time.",
                "Never using tools",
                "Using only one LLM call total",
                "Removing memory",
                "Creating a full plan first, then executing the steps"));
        q.add(new Question(A, "A common safeguard against agents looping forever is:", 1,
                "A maximum iteration/step limit (and timeouts) stops runaway agent loops.",
                "Raising the temperature",
                "A maximum number of iterations/steps",
                "Removing the system prompt",
                "Using a bigger embedding model"));
        q.add(new Question(A, "In tool calling, the tool's JSON schema tells the model:", 2,
                "The schema describes the tool's name, purpose and parameter types so the model can produce valid arguments.",
                "How to train itself",
                "The user's password",
                "The tool name, purpose and expected arguments",
                "Which GPU to use"));
        q.add(new Question(A, "Why apply 'least privilege' to an agent's tools?", 0,
                "Giving agents only the permissions they need limits damage from mistakes or prompt-injection attacks.",
                "To limit damage from errors or prompt injection",
                "To make the model smarter",
                "To increase the context window",
                "To speed up tokenization"));
        return q;
    }
}
