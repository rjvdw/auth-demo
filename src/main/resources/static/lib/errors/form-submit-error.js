import { HttpError } from './http-error.js'

/**
 * Represents a failed form submission.
 */
export class FormSubmitError extends HttpError {
  /** @type HTMLFormElement */
  #form

  /**
   * @param {HTMLFormElement} form
   * @param {Response} response
   */
  constructor(form, response) {
    super(response)

    this.#form = form
  }

  get form() {
    return this.#form
  }
}
