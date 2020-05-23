import {CustomFieldElement} from '@vaadin/vaadin-custom-field/src/vaadin-custom-field.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class FullWidthCustomFieldElement extends CustomFieldElement {
  static get template() {
    return html`
   <style>
      :host {
        display: inline-flex;
      }

      :host::before {
        content: "\\2003";
        width: 0;
        display: inline-block;
      }

      :host([hidden]) {
        display: none !important;
      }

      .container {
        width: 99%;
        display: flex;
        flex-direction: column;
      }

      .inputs-wrapper {
        display: block;
        flex: 1;
      }
    </style> 
    
    <label id="[[__labelId]]" on-click="focus" part="label">[[label]]</label> 
    <div class="inputs-wrapper" on-change="__updateValue"> 
     <slot id="slot"></slot> 
    </div> 
    <div aria-hidden\$="[[__getErrorMessageAriaHidden(invalid, errorMessage, __errorId)]]" aria-live="assertive" id="[[__errorId]]" part="error-message">
     [[errorMessage]] 
    </div> 
    
`;
  }

  static get is() {
    return 'full-width-custom-field';
  }
}

customElements.define(FullWidthCustomFieldElement.is, FullWidthCustomFieldElement);

export { FullWidthCustomFieldElement };

