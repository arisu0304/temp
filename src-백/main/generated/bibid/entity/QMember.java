package bibid.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -717781363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final StringPath addressDetail = createString("addressDetail");

    public final StringPath email = createString("email");

    public final StringPath memberAddress = createString("memberAddress");

    public final StringPath memberId = createString("memberId");

    public final NumberPath<Long> memberIndex = createNumber("memberIndex", Long.class);

    public final StringPath memberPnum = createString("memberPnum");

    public final StringPath memberPw = createString("memberPw");

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final QProfileImage profileImage;

    public final StringPath role = createString("role");

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profileImage = inits.isInitialized("profileImage") ? new QProfileImage(forProperty("profileImage"), inits.get("profileImage")) : null;
    }

}

