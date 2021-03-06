package grails.plugin.hibernatehijacker.template

import grails.plugin.hibernatehijacker.demo.Book
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.spock.IntegrationSpec

/**
 * @author Kim A. Betti
 */
@TestMixin(value = IntegrationTestMixin)
class HibernateTemplatesSpec extends IntegrationSpec {

    static transactional = false

    def sessionFactory
    def hibernateTemplates

    def "throwing a runtime exception rolls back the transaction"() {
        given:
        def book = new Book(name: "Groovy in action")
        book.save flush: true, failOnError: true

        when:
        hibernateTemplates.withTransaction {
            book.name = "Groovy in action 2"
            book.save flush: true, failOnError: true

            assert Book.findByName("Groovy in action 2") != null
            throw new RuntimeException("Should roll back the transaction")
        }

        then:
        Book.findByName("Groovy in action")
        !Book.findByName("Groovy in action 2")

        and:
        thrown(RuntimeException)
    }

    def "throwing a checked exception rolls back the transaction"() {
        given:
        def book = new Book(name: "Grails in action")
        book.save flush: true, failOnError: true

        when:
        hibernateTemplates.withTransaction {
            book.name = "Grails in action 2"
            book.save flush: true, failOnError: true

            assert Book.findByName("Grails in action 2") != null
            throw new Exception("Should roll back the transaction")
        }

        then:
        Book.findByName("Grails in action")
        !Book.findByName("Grails in action 2")

        and:
        thrown(Exception)
    }

    def "with new session"() {
        given:
        def currentSession = sessionFactory.getCurrentSession()

        when:
        hibernateTemplates.withNewSession { session ->
            assert session != currentSession
            new Book(name: "Another book").save(flush: true, failOnError: true)
        }

        then:
        Book.findByName("Another book")
    }

}
