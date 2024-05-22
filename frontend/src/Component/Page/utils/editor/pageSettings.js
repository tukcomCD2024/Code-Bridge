import { Schema } from "prosemirror-model";
import { schema as basicSchema } from "prosemirror-schema-basic";
import { addListNodes } from "prosemirror-schema-list";
import { defaultSettings, updateImageNode } from "prosemirror-image-plugin";

export const imageSettings = {
  ...defaultSettings,
  hasTitle: false,
  minSize: 30,
  maxSize: 550,
  extraAttributes: {
    'writer': localStorage.getItem("userId"),
    'data-guid': null,
  },
};

const imageNodeSpec = {
  inline: false,
  group: "block",
  attrs: {
    src: {},
    alt: { default: null },
    title: { default: null },
    guid: { default: null },
    writer: { default: null },
  },
  parseDOM: [
    {
      tag: "img[src]",
      getAttrs: (dom) => ({
        src: dom.getAttribute("src"),
        alt: dom.getAttribute("alt"),
        title: dom.getAttribute("title"),
        guid: dom.getAttribute("data-guid"),
        writer: dom.getAttribute("data-writer"),
      }),
    },
  ],
  toDOM: node => ["img", { ...node.attrs, "data-guid": node.attrs.guid, "data-writer": node.attrs.author }, 0],
};

const { nodes, marks } = basicSchema.spec;
const extendedNodes = addListNodes(
  nodes.append({ image: imageNodeSpec }),
  "paragraph block*",
  "block"
);

const customParagraphNode = {
  ...nodes.get("paragraph"),
  attrs: {
    ...nodes.get("paragraph").attrs,
    class: { default: "custom-paragraph" },
    guid: { default: "" }, // Ensure guid attribute is included
    writer: { default: localStorage.getItem("userId") },
  },
  parseDOM: [
    {
      tag: "p",
      getAttrs: (dom) => ({guid: dom.getAttribute("data-guid"), writer: dom.getAttribute("data-writer"),}),
    },
  ],
  toDOM(node) {
    return ["p", { class: node.attrs.class, "data-guid": node.attrs.guid, "data-writer": node.attrs.writer}, 0];
  },
};

const newParagraphNode = extendedNodes.update(
  "paragraph",
  customParagraphNode
);

const defaultNodes = updateImageNode(newParagraphNode, {
  ...imageSettings,
});

export const mySchema = new Schema({
  nodes: defaultNodes,
  marks: basicSchema.spec.marks,
});
